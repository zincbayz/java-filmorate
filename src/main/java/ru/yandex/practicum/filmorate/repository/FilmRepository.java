package ru.yandex.practicum.filmorate.repository;


import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.model.film.Genre;
import ru.yandex.practicum.filmorate.model.film.Mpa;

import java.util.List;

public interface FilmRepository {
    int createFilm(Film film);

    Film update(Film film, int id);

    List<Film> getAllFilms();

    List<Film> getPopularFilms(int count);

    Film getFilm(int id);

    void like(int filmId, int userId);

    int deleteLike(int filmId, long userId);

    List<Genre> getGenres();

    Genre getGenreById(int genreId);

    List<Mpa> getMpaRatings();

    Mpa getMpaById(int mpaId);

    void deleteFilmById(int id);

    List<Film> getCommonFilms(int userId, int friendId);

    List<Film> getSortedDirectorFilms(int directorId, String sortBy);

    void insertDirectorToFilm(int filmId, int directorId);

    List<Film> getMostPopulars(int limit, int genreId, int year);

    List<Film> searchFilms();

    List<Film> searchFilmsByDirector(String query);

    List<Film> searchFilmsByTitle(String query);

    List<Film> searchFilmsByDirectorAndTitle(String query);
}