package ru.yandex.practicum.filmorate.service;

import ru.yandex.practicum.filmorate.exception_handler.exeptions.EntityAllreadyExistExeption;
import ru.yandex.practicum.filmorate.exception_handler.exeptions.EntityNotFoundExeption;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.model.film.Genre;
import ru.yandex.practicum.filmorate.model.film.Mpa;

import java.sql.SQLException;
import java.util.List;

public interface FilmService {
    Film getFilm(int id);

    List<Film> getAllFilms();

    List<Film> getPopularFilms(int countTopFilms);

    Film create(Film film) throws SQLException;

    public Film update(Film film, int id);

    void like(int filmId, int userId) throws EntityAllreadyExistExeption, EntityNotFoundExeption;

    void deleteLike(int filmId, int userId) throws EntityNotFoundExeption;

    List<Genre> getGenres();

    Genre getGenreById(int genreId) throws EntityNotFoundExeption;

    List<Mpa> getMpaRatings();

    Mpa getMpaById(int mpaId);


    List<Film> searchFilms();

    List<Film> searchFilms(String query, List<String> by);

    List<Film> getSortedDirectorFilmsByYear(int directorId);

    List<Film> getSortedDirectorFilmsByLikes(int directorId);
}
