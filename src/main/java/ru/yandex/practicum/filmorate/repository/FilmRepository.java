package ru.yandex.practicum.filmorate.repository;


import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.model.film.Genre;
import ru.yandex.practicum.filmorate.model.film.Mpa;

import java.sql.SQLException;
import java.util.List;

public interface FilmRepository {
    Film createFilm(Film film) throws SQLException;

    Film update(Film film, int id) throws SQLException;

    List<Film> getAllFilms();

    List<Film> getPopularFilms(int count);

    Film getFilm(int id);

    void like(int filmId, int userId);

    int deleteLike(int filmId, long userId);

    List<Genre> getGenres();

    Genre getGenreById(int genreId);

    List<Mpa> getMpaRatings();

    Mpa getMpaById(int mpaId);

    List<Film> getSortedDirectorFilms(int directorId, String sortBy);

    void insertDirectorToFilm(int filmId, int directorId);

    List<Film> searchFilms();
    List<Film> searchFilmsByDirector(String query);
    List<Film> searchFilmsByTitle(String query);
    List<Film> searchFilmsByDirectorAndTitle(String query);

    boolean isUserExist(int userId);

    boolean isFilmExist(int filmId);

    boolean isLikeExist(int filmId, int userId);

    List<Film> getSortedDirectorFilmsByYear(int directorId);

    List<Film> getSortedDirectorFilmsByLikes(int directorId);
}