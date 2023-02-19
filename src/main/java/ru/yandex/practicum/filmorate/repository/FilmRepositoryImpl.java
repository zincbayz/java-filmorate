package ru.yandex.practicum.filmorate.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.BatchPreparedStatementSetter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.Util.DirectorMapper;
import ru.yandex.practicum.filmorate.Util.FilmMapper;
import ru.yandex.practicum.filmorate.Util.GenreMapper;
import ru.yandex.practicum.filmorate.Util.MpaMapper;
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

    @Override
    public List<Film> getAllFilms() {
        List<Film> filmsWithoutGenres = jdbcTemplate.query(ALL_FILMS_SQL_QUERY, new FilmMapper());

        List<Film> filmsWithGenres = addGenresInFilm(filmsWithoutGenres);
        return addDirectorToAllFilms(filmsWithGenres);
    }

    private List<Film> addDirectorToAllFilms(List<Film> filmsWithGenres) {
        final String genreQuery = "SELECT film_id, Films.director_id, director_name FROM Films JOIN Directors ON Films.director_id=Directors.director_id";
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
            film.setDirectors(directors);
        }
        return filmsWithGenres;
    }

    @Override
    public List<Film> getPopularFilms(int countTopFilms) {
        final String getTopFilmsQuery = ALL_FILMS_SQL_QUERY +
                "WHERE film_id IN (SELECT film_id FROM Likes GROUP BY film_id ORDER BY COUNT(user_id) DESC LIMIT ?)";
        List<Film> popularFilms = jdbcTemplate.query(getTopFilmsQuery, new FilmMapper(), countTopFilms);

        if (popularFilms.size() < countTopFilms) {
            List<Film> additionalFilms = getAllFilms();
            popularFilms.removeAll(additionalFilms);
            popularFilms.addAll(additionalFilms);
        }
        return popularFilms;
    }


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
    public void like(int filmId, int userId) {
        jdbcTemplate.update("INSERT INTO Likes(film_id, user_id) VALUES (?, ?)", filmId, userId);
    }

    public int deleteLike(int filmId, long userId) {
        String deleteQuery = "DELETE FROM Likes WHERE EXISTS(SELECT 1 FROM LIKES WHERE film_id=? AND user_id=?)";
        return jdbcTemplate.update(deleteQuery, filmId, userId);
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

    private Film updateFilmDirector(Film film, int filmId) {
        if(film.getDirectors() != null) {
            insertDirectorToFilm(filmId, film.getDirectors().get(0).getId());
            film.setDirectors(getDirectors(filmId));
        } else {
            jdbcTemplate.update("UPDATE Films SET director_id = null WHERE film_id = ?", filmId);
        }
        return film;
    }

    List<Genre> getAllFilmsGenres(int filmId) {
        final String genresQuery =
                "SELECT * FROM Film_Genre JOIN Genres ON Film_Genre.genre_id=Genres.genre_id WHERE film_id = ?";
        List<Genre> filmGenres = jdbcTemplate.query(genresQuery, new GenreMapper(), filmId);
        return filmGenres;
    }

    private Film updateFilmsGenre(Film film, int filmId) {
        if (film.getGenres() != null && film.getGenres().isEmpty()) {
            deleteGenre(filmId);
        } else {
            insertFilmsGenres(film, filmId);
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
