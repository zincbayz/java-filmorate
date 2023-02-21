package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.model.film.Genre;
import ru.yandex.practicum.filmorate.model.film.Mpa;

import java.util.List;

public interface FilmService {
    Film getFilm(int id);

    List<Film> getAllFilms();

    List<Film> getPopularFilms(int countTopFilms);

    List<Film> getMostPopulars(int limit, int genreId, int year);

    Film create(Film film);

    Film update(Film film, int id);

    void deleteFilmById(int id);

    void like(int filmId, int userId);

    void deleteLike(int filmId, long userId);

    List<Genre> getGenres();

    Genre getGenreById(int genreId);

    List<Mpa> getMpaRatings();

    Mpa getMpaById(int mpaId);

    List<Film> searchFilms();

    List<Film> searchFilms(String query, List<String> by);

    List<Film> getCommonFilms(int userId, int friendId);


}
