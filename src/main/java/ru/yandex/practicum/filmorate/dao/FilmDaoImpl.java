package ru.yandex.practicum.filmorate.dao;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.Util.FilmMapper;
import ru.yandex.practicum.filmorate.Util.GenreMapper;
import ru.yandex.practicum.filmorate.Util.MpaMapper;
import ru.yandex.practicum.filmorate.model.film.*;

import java.util.*;


@Slf4j
@Repository
public class FilmDaoImpl implements FilmDao {
    private static final String ALL_FILMS_SQL_QUERY = "SELECT * FROM Films JOIN Mpa ON Films.mpa_id=Mpa.mpa_id ";
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public FilmDaoImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Film getFilm(int id) throws EmptyResultDataAccessException {
         Film film = jdbcTemplate.queryForObject(ALL_FILMS_SQL_QUERY + "WHERE film_id = ?",
                new FilmMapper(), id);
         film.setGenres(getAllFilmsGenres(id));
        return film;
    }

    @Override
    public List<Film> getAllFilms() {
        List<Film> all = jdbcTemplate.query(ALL_FILMS_SQL_QUERY, new FilmMapper());
        for (Film film : all) {
            film.setGenres(getAllFilmsGenres(film.getId()));
        }
        return all;
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

        for (Film film : popularFilms) {
            film.setGenres(getAllFilmsGenres(film.getId()));
        }
        return popularFilms;
    }


    @Override
    public Film createFilm(Film film) {
        int filmId = insertFilm(film);

        log.info("Film added: " + film.getName());

        Film createdFilm = getFilm(filmId);
        createdFilm.setGenres(getAllFilmsGenres(filmId));

        return createdFilm;
    }

    @Override
    public Film update(Film film, int filmId) {
        final String updateQuery = "UPDATE Films SET name=?, description=?, releaseDate=?, duration=?, mpa_id=? WHERE film_id=?";
        jdbcTemplate.update(updateQuery,
                film.getName(), film.getDescription(), film.getReleaseDate(),
                film.getDuration(), film.getMpa().getId(), filmId);
        log.info("Film updated: " + film.getName());

        return updateFilmsGenre(film, filmId);
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
        Mpa mpa = jdbcTemplate.queryForObject("SELECT * FROM Mpa WHERE mpa_id=?",
                new MpaMapper(), mpaId);
        return mpa;
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

        int filmId = getInsertedFilmId();

        insertFilmsGenres(film, filmId);

        return filmId;
    }

    private int getInsertedFilmId() {
        int filmId = 0;
        SqlRowSet filmRows = jdbcTemplate.queryForRowSet("SELECT film_id FROM Films ORDER BY film_id DESC LIMIT 1");
        if (filmRows.next()) {
            return Integer.parseInt(filmRows.getString("film_id"));
        }
        return filmId;
    }

    private void insertFilmsGenres(Film film, int filmId) {
        final String insertGenres = "INSERT INTO Film_Genre(film_id, genre_id) VALUES(?, ?)";
        if (film.getGenres() != null) {
            Set<Genre> uniqueGenres = new TreeSet<>(Comparator.comparing(Genre::getId));
            uniqueGenres.addAll(film.getGenres());
            deleteGenre(filmId);
            for (Genre genre : uniqueGenres) {
                jdbcTemplate.update(insertGenres, filmId, genre.getId());
            }
        }
    }

    private List<Genre> getAllFilmsGenres(int filmId) {
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
        return getFilm(filmId);
    }

    private void deleteGenre(int filmId) {
        String deleteGenreQuery = "DELETE FROM Film_Genre WHERE EXISTS(SELECT 1 FROM Film_Genre WHERE film_id=?)";
        jdbcTemplate.update(deleteGenreQuery, filmId);
    }

}
