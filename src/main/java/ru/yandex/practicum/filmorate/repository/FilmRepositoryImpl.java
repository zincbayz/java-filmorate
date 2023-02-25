package ru.yandex.practicum.filmorate.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.exception_handler.exceptions.RequiredObjectWasNotFound;
import ru.yandex.practicum.filmorate.util.DirectorMapper;
import ru.yandex.practicum.filmorate.util.FilmMapper;
import ru.yandex.practicum.filmorate.util.GenreMapper;
import ru.yandex.practicum.filmorate.util.MpaMapper;
import ru.yandex.practicum.filmorate.model.film.*;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Repository
public class FilmRepositoryImpl implements FilmRepository {
    private static final String ALL_FILMS_SQL_QUERY = "SELECT * FROM Films " +
            "JOIN Mpa ON Films.mpa_id=Mpa.mpa_id ";
    private static final int FILM_DOESNT_EXIST = 0;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public FilmRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Film getFilm(int id) throws EmptyResultDataAccessException {
        Film film = jdbcTemplate.queryForObject(ALL_FILMS_SQL_QUERY + "WHERE film_id = ?",
                new FilmMapper(), id);
        film.setGenres(getAllFilmsGenres(id));
        film.setDirectors(getDirectors(id));
        return film;
    }

    @Override
    public List<Film> getAllFilms() {
        List<Film> filmsWithoutGenres = jdbcTemplate.query(ALL_FILMS_SQL_QUERY, new FilmMapper());
        List<Film> filmsWithGenres = addGenresInFilm(filmsWithoutGenres);
        return addDirectorToAllFilms(filmsWithGenres);
    }

    @Override
    public List<Film> getPopularFilms(int countTopFilms) {
        final String getTopFilmsQuery = ALL_FILMS_SQL_QUERY +
                "WHERE film_id IN (SELECT film_id FROM Likes GROUP BY film_id ORDER BY COUNT(user_id) DESC LIMIT ?)";
        List<Film> popularFilmsWithGenres = jdbcTemplate.query(getTopFilmsQuery, new FilmMapper(), countTopFilms);
        return addGenresInFilm(popularFilmsWithGenres);
    }

    @Override
    public List<Film> getMostPopularsByYear(String startYear, String endYear, int limit) {
        List<Film> mostPopularFilms = jdbcTemplate.query(ALL_FILMS_SQL_QUERY +
                        "WHERE releaseDate BETWEEN DATE '" + startYear + "' AND DATE '" + endYear + "' LIMIT ?",
                new FilmMapper(), limit);
        return addGenresInFilm(mostPopularFilms);
    }
    @Override
    public List<Film> getMostPopularsByGenre(int genreId, int limit) {
        List<Film> mostPopularFilms = jdbcTemplate.query(ALL_FILMS_SQL_QUERY +
                "WHERE film_id IN (SELECT film_id FROM Film_Genre WHERE genre_id = ? LIMIT ?)",
                new FilmMapper(), genreId, limit);
        return addGenresInFilm(mostPopularFilms);
    }
    @Override
    public List<Film> getMostPopularsByYearAndGenre(int genreId, String startYear, String endYear, int limit) {
        List<Film> mostPopularFilms = jdbcTemplate.query(ALL_FILMS_SQL_QUERY +
                "WHERE film_id IN (SELECT film_id FROM Film_Genre WHERE genre_id = ?) " +
                "AND releaseDate BETWEEN DATE '" + startYear + "' AND DATE '" + endYear + "' LIMIT ?",
                new FilmMapper(), genreId, limit);
        return addGenresInFilm(mostPopularFilms);
    }

    @Override
    public List<Film> getCommonFilms(int userId, int friendId) {
        String sql = "SELECT f.*, M.* " +
                "FROM LIKES " +
                "JOIN LIKES l ON l.FILM_ID = LIKES.FILM_ID " +
                "JOIN FILMS f on f.film_id = l.film_id " +
                "JOIN MPA M on f.mpa_id = M.MPA_ID " +
                "WHERE l.USER_ID = ? AND LIKES.USER_ID = ?";

        return jdbcTemplate.query(sql, new FilmMapper(), userId, friendId);
    }

    @Override
    public int createFilm(Film film) {
        int filmId = insertFilm(film);
        insertDirectorToFilm(filmId, film.getDirectors());
        insertFilmsGenres(film.getGenres(), filmId);
        log.info("Film added: " + film.getName());
        return filmId;
    }

    @Override
    public Film update(Film film, int filmId) {
        final String updateQuery = "UPDATE Films SET name=?, description=?, releaseDate=?, duration=?, mpa_id=? WHERE film_id=?";
        jdbcTemplate.update(updateQuery,
                film.getName(), film.getDescription(), film.getReleaseDate(),
                film.getDuration(), film.getMpa().getId(), filmId);
        log.info("Film updated: " + film.getName());

        Film filmWithDirector = updateFilmDirector(film, filmId);
        updateFilmsGenre(filmWithDirector, filmId);
        return getFilm(filmId);
    }

    @Override
    public void deleteFilmById(int id) {
        final String sqlQuery = "DELETE FROM films WHERE FILM_ID = ?";
        jdbcTemplate.update(sqlQuery, id);
    }


    @Override
    public void like(int filmId, int userId) {
        String likeQuery2 = "MERGE INTO Likes USING (SELECT CAST(? AS int) AS film_id, CAST(? AS int) AS user_id) AS TMP " +
                "ON Likes.film_id=TMP.film_id AND Likes.user_id=TMP.user_id " +
                "WHEN NOT MATCHED THEN INSERT VALUES(TMP.film_id, TMP.user_id)";
        increaseFilmRate(filmId);
        jdbcTemplate.update(likeQuery2, filmId, userId);
    }

    @Override
    public int deleteLike(int filmId, int userId) {
        String deleteQuery = "DELETE FROM Likes WHERE EXISTS(SELECT 1 FROM LIKES WHERE film_id=? AND user_id=?)";
        decreaseFilmRate(filmId);
        return jdbcTemplate.update(deleteQuery, filmId, userId);
    }
    private void increaseFilmRate(int filmId) {
        String sqlQuery = "UPDATE FILMS SET rate = rate + 1 WHERE film_id=?";
        jdbcTemplate.update(sqlQuery, filmId);
    }

    private void decreaseFilmRate(int filmId) {
        String sqlQuery = "UPDATE FILMS SET rate = rate - 1 WHERE film_id=?";
        jdbcTemplate.update(sqlQuery, filmId);
    }

    @Override
    public List<Genre> getGenres() {
        return jdbcTemplate.query("SELECT * FROM Genres", new GenreMapper());
    }

    @Override
    public Genre getGenreById(int genreId) {
        return jdbcTemplate.queryForObject("SELECT * FROM Genres WHERE genre_id=?",
                new GenreMapper(), genreId);
    }


    @Override
    public List<Mpa> getMpaRatings() {
        return jdbcTemplate.query("SELECT * FROM Mpa", new MpaMapper());
    }

    @Override
    public Mpa getMpaById(int mpaId) {
        return jdbcTemplate.queryForObject("SELECT * FROM Mpa WHERE mpa_id=?",
                new MpaMapper(), mpaId);
    }

    @Override
    public List<Film> searchFilms() {
        final String sql = "SELECT * " +
                "FROM FILMS f " +
                "INNER JOIN MPA m ON f.mpa_id = m.mpa_id " +
                "WHERE f.film_id IN " +
                "(SELECT film_id FROM LIKES GROUP BY film_id ORDER BY COUNT(user_id) DESC) ";
        List<Film> searchFilms = addGenresInFilm(jdbcTemplate.query(sql, new FilmMapper()));
        return addDirectorToAllFilms(searchFilms);
    }

    @Override
    public List<Film> searchFilmsByDirector(String query) {
        final String sql = "SELECT * " +
                "FROM FILMS f " +
                "INNER JOIN MPA m ON f.mpa_id = m.mpa_id  " +
                "INNER JOIN FILM_DIRECTOR fd ON f.FILM_ID  = fd.FILM_ID " +
                "INNER JOIN DIRECTORS d ON fd.DIRECTOR_ID = d.DIRECTOR_ID " +
                "WHERE LOWER(DIRECTOR_NAME) LIKE ?";
        List<Film> searchFilms = addGenresInFilm(jdbcTemplate.query(sql, new FilmMapper(), new String[] {"%" + query + "%"}));
        return addDirectorToAllFilms(searchFilms);
    }

    @Override
    public List<Film> searchFilmsByTitle(String query) {
        final String sql = "SELECT * " +
                "FROM FILMS f " +
                "INNER JOIN MPA m ON f.mpa_id = m.mpa_id " +
                "WHERE LOWER(NAME) LIKE ?";
        List<Film> searchFilms = addGenresInFilm(jdbcTemplate.query(sql, new FilmMapper(), new String[] {"%" + query + "%"}));
        return addDirectorToAllFilms(searchFilms);
    }

    @Override
    public List<Film> searchFilmsByDirectorAndTitle(String query) {
        final String sql = "SELECT f.FILM_ID, f.NAME, f.DESCRIPTION, f.RELEASEDATE, f.DURATION, f.RATE, f.MPA_ID, m.mpa_name  " +
                "FROM FILMS f " +
                "INNER JOIN MPA m ON f.mpa_id = m.mpa_id " +
                " WHERE  LOWER(NAME) LIKE ? " +
                "UNION " +
                "SELECT f.FILM_ID, f.NAME, f.DESCRIPTION, f.RELEASEDATE, f.DURATION, f.RATE, f.MPA_ID, m.mpa_name " +
                "FROM FILMS f " +
                "INNER JOIN MPA m ON f.mpa_id = m.mpa_id  " +
                "INNER JOIN FILM_DIRECTOR fd ON f.FILM_ID  = fd.FILM_ID " +
                "INNER JOIN DIRECTORS d ON fd.DIRECTOR_ID = d.DIRECTOR_ID  " +
                "WHERE LOWER(DIRECTOR_NAME) LIKE ? " +
                "ORDER BY RATE DESC;";
        List<Film> searchFilms = addGenresInFilm(jdbcTemplate.query(sql, new FilmMapper(), new String[] {"%" + query + "%","%" + query + "%"}));
        return addDirectorToAllFilms(searchFilms);
    }

    @Override
    public List<Film> getSortedDirectorFilms(int directorId, String sqlQuery) {
        List<Film> sortedDirectorFilms = jdbcTemplate.query(sqlQuery,
                new FilmMapper(), directorId);

        List<Film> sortedFilmsWithGenres = addGenresInFilm(sortedDirectorFilms);
        return addDirectorToAllFilms(sortedFilmsWithGenres);
    }

    @Override
    public void insertDirectorToFilm(int filmId, List<Director> directors) {
        if(directors != null) {
            jdbcTemplate.update("INSERT INTO Film_Director (film_id, director_id) VALUES (?, ?)", filmId, directors.get(0).getId());
        }
    }

    List<Genre> getAllFilmsGenres(int filmId) {
        final String genresQuery =
                "SELECT * FROM Film_Genre JOIN Genres ON Film_Genre.genre_id=Genres.genre_id WHERE film_id = ?";
        return jdbcTemplate.query(genresQuery, new GenreMapper(), filmId);
    }

    private List<Director> getDirectors(int filmId) {
        return jdbcTemplate.query("SELECT * FROM FILM_DIRECTOR " +
                        "Join Directors ON FILM_DIRECTOR.DIRECTOR_ID = DIRECTORS.DIRECTOR_ID " +
                        "WHERE film_id=? AND DIRECTORS.DIRECTOR_ID IS NOT NULL",
                new DirectorMapper(), filmId);
    }

    private void insertFilmsGenres(List<Genre> genres, int filmId) {
        if (genres != null) {
            List<Integer> genresId = genres.stream()
                    .map(Genre::getId)
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());
            deleteGenre(filmId);
            jdbcTemplate.batchUpdate("INSERT INTO Film_Genre VALUES(?, ?)", new BatchPreparedStatementSetter() {
                @Override
                public void setValues(PreparedStatement ps, int i) throws SQLException {
                    ps.setInt(1, filmId);
                    ps.setInt(2, genresId.get(i));
                }

                @Override
                public int getBatchSize() {
                    return genresId.size();
                }
            });
        }
    }

    private int insertFilm(Film film) {
        final String insertSql = "INSERT INTO Films(name, description, releaseDate, duration, mpa_id) " +
                "VALUES (?, ?, ?, ?, ?)";

        jdbcTemplate.update(insertSql,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getMpa().getId());

        return getInsertedFilmId();
    }

    private List<Film> addDirectorToAllFilms(List<Film> filmsWithGenres) {
        final String genreQuery = "SELECT film_id, DIRECTORS.director_id, DIRECTORS.director_name FROM FILM_DIRECTOR JOIN Directors ON FILM_DIRECTOR.director_id=Directors.director_id";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(genreQuery);

        for (Film film : filmsWithGenres) {
            List<Director> directors = rows.stream()
                    .filter(stringObjectMap -> (int) stringObjectMap.get("FILM_ID") == film.getId())
                    .map(stringObjectMap -> {
                        Director director = new Director();
                        director.setId((Integer) stringObjectMap.get("DIRECTOR_ID"));
                        director.setName((String) stringObjectMap.get("DIRECTOR_NAME"));
                        return director;
                    })
                    .collect(Collectors.toList());
            film.getDirectors().addAll(directors);
        }
        return filmsWithGenres;
    }

    private int getInsertedFilmId() {
        int filmId = 0;
        SqlRowSet filmRows = jdbcTemplate.queryForRowSet("SELECT film_id FROM Films ORDER BY film_id DESC LIMIT 1");
        if (filmRows.next()) {
            return Integer.parseInt(filmRows.getString("film_id"));
        }
        return filmId;
    }

    private Film updateFilmDirector(Film film, int filmId) {
        if(film.getDirectors() != null) {
            insertDirectorToFilm(filmId, film.getDirectors());
            film.setDirectors(getDirectors(filmId));
        } else {
            jdbcTemplate.update("UPDATE FILM_DIRECTOR SET director_id = null WHERE film_id = ?", filmId);
        }
        return film;
    }


    private void updateFilmsGenre(Film film, int filmId) {
        if (film.getGenres() != null && film.getGenres().isEmpty()) {
            deleteGenre(filmId);
        } else {
            insertFilmsGenres(film.getGenres(), filmId);
        }
    }

    private void deleteGenre(int filmId) {
        String deleteGenreQuery = "DELETE FROM Film_Genre WHERE EXISTS(SELECT 1 FROM Film_Genre WHERE film_id=?)";
        jdbcTemplate.update(deleteGenreQuery, filmId);
    }

    List<Film> addGenresInFilm(List<Film> films) {
        final String genreQuery = "SELECT * FROM Film_Genre JOIN Genres ON Film_Genre.genre_id=Genres.genre_id";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(genreQuery);

        for (Film film : films) {
            List<Genre> allFilmsGenres = rows.stream()
                    .filter(stringObjectMap -> (int)stringObjectMap.get("FILM_ID") == film.getId())
                    .map(stringObjectMap -> {
                        Genre genre = new Genre();
                        genre.setId((Integer) stringObjectMap.get("GENRE_ID"));
                        genre.setName((String) stringObjectMap.get("NAME"));
                        return genre;
                    })
                    .collect(Collectors.toList());
            film.setGenres(allFilmsGenres);
        }
        return films;
    }

    public void isFilmExist(int filmId) {
        int count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM Films where film_id=?",
                new Object[] { filmId }, Integer.class);
        if (count == FILM_DOESNT_EXIST) {
            throw new RequiredObjectWasNotFound("User or Film not found");
        }
    }
}
