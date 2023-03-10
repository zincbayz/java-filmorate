package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception_handler.RequiredObjectWasNotFound;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.model.film.Genre;
import ru.yandex.practicum.filmorate.model.film.Mpa;
import ru.yandex.practicum.filmorate.repository.FilmRepository;

import java.util.List;

public interface FilmService {
    Film getFilm(int id);

    List<Film> getAllFilms();

    List<Film> getPopularFilms(int countTopFilms);

    Film create(Film film);

    public Film update(Film film, int id);

    void like(int filmId, int userId);

    void deleteLike(int filmId, long userId);

    List<Genre> getGenres();

    Genre getGenreById(int genreId);

    List<Mpa> getMpaRatings();

    Mpa getMpaById(int mpaId);


}
