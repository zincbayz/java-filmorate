package ru.yandex.practicum.filmorate.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.Util.DirectorMapper;
import ru.yandex.practicum.filmorate.Util.FilmMapper;
import ru.yandex.practicum.filmorate.Util.GenreMapper;
import ru.yandex.practicum.filmorate.Util.MpaMapper;
import ru.yandex.practicum.filmorate.model.film.*;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.stream.Collectors;


@Slf4j
@Repository
public class FilmRepositoryImpl implements FilmRepository {
    private final static  int  SQLBATCHSIZE = 100;
    private final SimpleJdbcInsert insertIntoFilm;
    private static final String ALL_FILMS_SQL_QUERY = "SELECT * FROM Films JOIN Mpa ON Films.mpa_id=Mpa.mpa_id ";
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public FilmRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
        insertIntoFilm = new SimpleJdbcInsert(this.jdbcTemplate).withTableName("films").usingGeneratedKeyColumns("film_id");

    }


    public List<Director> getDirectors(int filmId) {
        String sqlQuery = "SELECT * FROM directors WHERE director_id IN ( SELECT DISTINCT director_id FROM film_director WHERE film_id = ?)";
        List directors = new ArrayList<>();
        directors = jdbcTemplate.query(sqlQuery,
                (rs, rowNum) ->
                        new Director(
                                rs.getInt("director_id"),
                                rs.getString("director_name")),
                filmId);
        log.info("Запрошен список директоров : " + directors.toString());
        return directors;
    }

/*
    private List<Director> getDirectors(int id) {
        String directorId = jdbcTemplate.queryForObject("SELECT director_id FROM Films WHERE film_id=?",
                new Object[]{id}, String.class);
        if(directorId != null) {
            Director director = jdbcTemplate.queryForObject("SELECT * FROM Directors WHERE director_id=?",
                    new DirectorMapper(), directorId);
            return Collections.singletonList(director);
        }
        return Collections.emptyList();
    }
*/
    @Override
    public List<Film> getAllFilms() {
        List<Film> filmsWithoutGenres = jdbcTemplate.query(ALL_FILMS_SQL_QUERY, new FilmMapper());
        List<Film> filmsWithGenres = addGenresInFilm(filmsWithoutGenres);
        return addDirectorToAllFilms(filmsWithGenres);
    }

    private List<Film> addDirectorToAllFilms(List<Film> filmsWithGenres) {
        final String genreQuery = "SELECT film_id, Films.director_id, director_name FROM Films JOIN Directors ON Films.director_id=Directors.director_id";
        final String sql = "SELECT f.film_id, fd.director_id, d.director_name " +
                "FROM  FILMS f " +
                "INNER JOIN FILM_DIRECTOR fd ON f.film_id = fd.film_id " +
                "INNER JOIN DIRECTORS d ON fd.director_id=d.director_id";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(sql);

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
            film.setDirectors(directors);
        }
        return filmsWithGenres;
    }


    @Override
    public List<Film> getPopularFilms(int countTopFilms) {
        final String getTopFilmsQuery = "SELECT * FROM Films " +
                "JOIN Mpa ON Films.mpa_id=Mpa.mpa_id " +
                "ORDER BY rate DESC " +
                "LIMIT ?";
        List<Film> popularFilmsWithoutGenres = addGenresInFilm(jdbcTemplate.query(getTopFilmsQuery, new FilmMapper(), countTopFilms));
        return addDirectorToAllFilms(popularFilmsWithoutGenres);

        /*if (popularFilms.size() < countTopFilms) {
            List<Film> additionalFilms = getAllFilms();
            popularFilms.removeAll(additionalFilms);
            popularFilms.addAll(additionalFilms);
        }*/
    }


 /*
    @Override
    public int createFilm(Film film) {
        int filmId = insertFilm(film);
        log.info("Film added: " + film.getName());
        if(film.getDirectors() != null) {
            insertDirectorToFilm(filmId, film.getDirectors().get(0).getId());
        }
        insertFilmsGenres(film, filmId);
        return filmId;
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
*/
     public Film getFilm(int id) throws EmptyResultDataAccessException {
     Film film = jdbcTemplate.queryForObject("SELECT * FROM Films JOIN Mpa ON Films.mpa_id=Mpa.mpa_id WHERE film_id = ?",
             new FilmMapper(), id);
     film.setGenres(getAllFilmsGenres(id));
     film.setDirectors(getDirectors(id));
     log.info("Запроше фильм id : " + film.toString());
     return film;
     }
    @Override
    @Transactional
    public Film createFilm(Film film) throws SQLException {
        final Map<String, Object> parameters = new HashMap<>();
        parameters.put("name", film.getName());
        parameters.put("description", film.getDescription());
        parameters.put("releaseDate", film.getReleaseDate());
        parameters.put("duration", film.getDuration());
        parameters.put("rate", film.getRate());
        parameters.put("mpa_id", film.getMpa().getId());
        int filmId = (int) insertIntoFilm.executeAndReturnKey(parameters);
        if (film.getGenres() != null) {
            addGenresByFilmId(filmId, film.getGenres());// добавляем жанры к фильму
        }
        if (film.getDirectors() != null) {
            addDirectorsByFilmId(filmId, film.getDirectors()); // добавляем режиссеров к фильму
        }
        log.info("Добавлен фильм : " + film.toString());
        return getFilm(filmId);
    }

    @Transactional
    public void addGenresByFilmId(int filmId, List<Genre> genres) throws SQLException {
        DataSource ds = jdbcTemplate.getDataSource();
        Connection connection = ds.getConnection();
        connection.setAutoCommit(false);
        String sqlQuery = "INSERT INTO FILM_GENRE (film_id, genre_id) values (?,?)";
        jdbcTemplate.batchUpdate(sqlQuery,
                genres,
                SQLBATCHSIZE,
                (PreparedStatement ps, Genre genre) -> {
                    ps.setLong(1, filmId);
                    ps.setLong(2, genre.getId());
                });
    }

    @Transactional
    public void addDirectorsByFilmId(int filmId, List<Director> directors) throws SQLException {
        DataSource ds = jdbcTemplate.getDataSource();
        Connection connection = ds.getConnection();
        connection.setAutoCommit(false);
        String sqlQuery = "INSERT INTO FILM_DIRECTOR (film_id, director_id) values (?,?)";
        jdbcTemplate.batchUpdate(sqlQuery,
                directors,
                SQLBATCHSIZE,
                (PreparedStatement ps, Director director) -> {
                    ps.setLong(1, filmId);
                    ps.setLong(2, director.getId());
                });
    }

    @Override
    @Transactional
    public Film update(Film film, int filmId) throws SQLException {
        final String updateQuery = "UPDATE Films SET name=?, description=?, releaseDate=?, duration=?, rate=?, mpa_id=? WHERE film_id=?";
        jdbcTemplate.update(updateQuery,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                film.getRate(),
                film.getMpa().getId(),
                filmId);
        log.info("Добавлен фильм : " + film.toString());

        updateFilmDirector(film, filmId);
        updateFilmsGenre(film, filmId);
        log.info("Изменен фильм : " + film.toString());
        return getFilm(filmId);
    }

    @Override
    public void like(int filmId, int userId) {
        String insertQuery = "INSERT INTO Likes(film_id, user_id) VALUES (?, ?)";
        increaseFilmRate(filmId);
        jdbcTemplate.update(insertQuery, filmId, userId);

    }

    @Transactional
    public int deleteLike(int filmId, long userId) {
        String deleteQuery = "DELETE FROM Likes WHERE EXISTS(SELECT 1 FROM LIKES WHERE film_id=? AND user_id=?)";
        decreaseFilmRate(filmId);
        return jdbcTemplate.update(deleteQuery, filmId, userId);

    }

    @Transactional
    public boolean increaseFilmRate(int filmId) {
        String sqlQuery = "UPDATE FILMS SET rate = rate + 1 WHERE film_id=?";
        return jdbcTemplate.update(sqlQuery, filmId) > 0;
    }
    @Transactional
    public boolean decreaseFilmRate(int filmId) {
        String sqlQuery = "UPDATE FILMS SET rate = rate - 1 WHERE film_id=?";
        return jdbcTemplate.update(sqlQuery, filmId) > 0;
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
    public List<Film> getSortedDirectorFilms(int directorId, String sqlQuery) {
        List<Film> sortedDirectorFilms = jdbcTemplate.query(sqlQuery,
                new FilmMapper(), directorId);

        List<Film> sortedFilmsWithGenres = addGenresInFilm(sortedDirectorFilms);
        return addDirectorToAllFilms(sortedFilmsWithGenres);
    }

    @Override
    public void insertDirectorToFilm(int filmId, int directorId) {
        jdbcTemplate.update("UPDATE Films SET director_id = ? WHERE film_id = ?", directorId, filmId);
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
                "WHERE DIRECTOR_NAME LIKE ?";
        List<Film> searchFilms = addGenresInFilm(jdbcTemplate.query(sql, new FilmMapper(), new String[] {"%" + query + "%"}));
        return addDirectorToAllFilms(searchFilms);
    }

    @Override
    public List<Film> searchFilmsByTitle(String query) {
        final String sql = "SELECT * " +
                "FROM FILMS f " +
                "INNER JOIN MPA m ON f.mpa_id = m.mpa_id " +
                "WHERE name LIKE ?";
        List<Film> searchFilms = addGenresInFilm(jdbcTemplate.query(sql, new FilmMapper(), new String[] {"%" + query + "%"}));
        return addDirectorToAllFilms(searchFilms);
    }

    @Override
    public List<Film> searchFilmsByDirectorAndTitle(String query) {
        final String sql = "SELECT f.FILM_ID, f.NAME, f.DESCRIPTION, f.RELEASEDATE, f.DURATION, f.MPA_ID, m.mpa_name " +
                "FROM FILMS f " +
                "INNER JOIN MPA m ON f.mpa_id = m.mpa_id  " +
                "INNER JOIN FILM_DIRECTOR fd ON f.FILM_ID  = fd.FILM_ID " +
                "INNER JOIN DIRECTORS d ON fd.DIRECTOR_ID = d.DIRECTOR_ID  " +
                "WHERE DIRECTOR_NAME LIKE ? " +
                "UNION " +
                "SELECT f.FILM_ID, f.NAME, f.DESCRIPTION, f.RELEASEDATE, f.DURATION, f.MPA_ID, m.mpa_name  " +
                "FROM FILMS f " +
                "INNER JOIN MPA m ON f.mpa_id = m.mpa_id " +
                " WHERE NAME LIKE ?";
        List<Film> searchFilms = addGenresInFilm(jdbcTemplate.query(sql, new FilmMapper(), new String[] {"%" + query + "%","%" + query + "%"}));
        return addDirectorToAllFilms(searchFilms);
    }

    @Override
    public boolean isUserExist(int userId) {
        String sqlQuery = "SELECT 1 FROM USERS WHERE user_id=?";
        return Boolean.TRUE.equals(jdbcTemplate.query(sqlQuery,
                (ResultSet rs) -> {
                    if (rs.next()) {
                        return true;
                    }
                    return false;
                }, userId
        ));
    }

    @Override
    public boolean isFilmExist(int filmId) {
        String sqlQuery = "SELECT 1 FROM FILMS WHERE film_id=?";
        return Boolean.TRUE.equals(jdbcTemplate.query(sqlQuery,
                (ResultSet rs) -> {
                    if (rs.next()) {
                        return true;
                    }
                    return false;
                }, filmId
        ));
    }

    @Override
    public boolean isLikeExist(int filmId, int userId) {
        String sqlQuery = "SELECT 1 FROM LIKES WHERE film_id=? AND user_id=?";
        return Boolean.TRUE.equals(jdbcTemplate.query(sqlQuery,
                (ResultSet rs) -> {
                    if (rs.next()) {
                        return true;
                    }
                    return false;
                }, new Object[]{filmId, userId}
        ));
    }

    @Override
    public List<Film> getSortedDirectorFilmsByYear(int directorId) {
        final String sql = "SELECT * " +
                "FROM FILMS f " +
                "INNER JOIN MPA m ON f.mpa_id = m.mpa_id  " +
                "INNER JOIN FILM_DIRECTOR fd ON f.FILM_ID  = fd.FILM_ID " +
                "INNER JOIN DIRECTORS d ON fd.DIRECTOR_ID = d.DIRECTOR_ID " +
                "WHERE DIRECTOR_ID = ?" +
                "ORDER BY RELEASEDATE";
        List<Film> searchFilms = addGenresInFilm(jdbcTemplate.query(sql, new FilmMapper(), directorId));
        return addDirectorToAllFilms(searchFilms);
    }

    @Override
    public List<Film> getSortedDirectorFilmsByLikes(int directorId) {
        final String sql = "SELECT * " +
                "FROM FILMS f " +
                "INNER JOIN MPA m ON f.mpa_id = m.mpa_id  " +
                "INNER JOIN FILM_DIRECTOR fd ON f.FILM_ID  = fd.FILM_ID " +
                "INNER JOIN DIRECTORS d ON fd.DIRECTOR_ID = d.DIRECTOR_ID " +
                "WHERE DIRECTOR_ID = ?" +
                "ORDER BY RATE";
        List<Film> searchFilms = addGenresInFilm(jdbcTemplate.query(sql, new FilmMapper(), directorId));
        return addDirectorToAllFilms(searchFilms);
    }

    private void insertFilmsGenres(Film film, int filmId) {
        final String insertGenres = "INSERT INTO Film_Genre VALUES(?, ?)";

        if (film.getGenres() != null) {
            List<Integer> genresId = film.getGenres().stream()
                    .map(Genre::getId)
                    .distinct()
                    .sorted()
                    .collect(Collectors.toList());
            deleteGenre(filmId);
            jdbcTemplate.batchUpdate(insertGenres, new BatchPreparedStatementSetter() {
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

    private List<Genre> getAllFilmsGenres(int filmId) {
        final String genresQuery =
                "SELECT * FROM Film_Genre JOIN Genres ON Film_Genre.genre_id=Genres.genre_id WHERE film_id = ?";
        return jdbcTemplate.query(genresQuery, new GenreMapper(), filmId);
    }

    private int getInsertedFilmId() {
        int filmId = 0;
        SqlRowSet filmRows = jdbcTemplate.queryForRowSet("SELECT film_id FROM Films ORDER BY film_id DESC LIMIT 1");
        if (filmRows.next()) {
            return Integer.parseInt(filmRows.getString("film_id"));
        }
        return filmId;
    }

    private void updateFilmDirector(Film film, int filmId) throws SQLException {
        jdbcTemplate.update("DELETE FROM film_director WHERE film_id = ?", filmId );
        if (film.getDirectors() != null) {
            addDirectorsByFilmId(filmId, film.getDirectors());
        }

    }

    private void updateFilmsGenre(Film film, int filmId) throws SQLException {
        jdbcTemplate.update("DELETE FROM film_genre WHERE film_id = ?", filmId );
        if ((film.getGenres()) != null && (!film.getGenres().isEmpty())) {
            addGenresByFilmId(filmId,
                    film.getGenres()
                    .stream()
                    .distinct()
                    .collect(Collectors.toList())); // список жанров без дубликатов
        }
    }

    private void deleteGenre(int filmId) {
        String deleteGenreQuery = "DELETE FROM Film_Genre WHERE EXISTS(SELECT 1 FROM Film_Genre WHERE film_id=?)";
        jdbcTemplate.update(deleteGenreQuery, filmId);
    }

    private List<Film> addGenresInFilm(List<Film> films) {
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
}
