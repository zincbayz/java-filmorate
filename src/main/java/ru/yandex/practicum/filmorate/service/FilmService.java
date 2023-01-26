package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.dao.FilmDao;
import ru.yandex.practicum.filmorate.exception_handler.RequiredObjectWasNotFound;
import ru.yandex.practicum.filmorate.model.film.Genre;
import ru.yandex.practicum.filmorate.model.film.Mpa;

import java.util.List;

@Slf4j
@Service
public class FilmService {

    private final FilmDao filmDao;
    @Autowired
    public FilmService(@Qualifier("filmDaoImpl")FilmDao filmDao) {
        this.filmDao = filmDao;
    }


    public Film getFilm(int id) {
        try {
            return filmDao.getFilm(id);
        } catch (EmptyResultDataAccessException e) {
            throw new RequiredObjectWasNotFound("Film " + id + " not found");
        }

    }

    public List<Film> getAllFilms() {
        return filmDao.getAllFilms();
    }

    public List<Film> getPopularFilms(int countTopFilms) {
        return filmDao.getPopularFilms(countTopFilms);
    }


    public Film create(Film film) {
       return filmDao.createFilm(film);
    }

    public Film update(Film film, int id) {
        try {
            return filmDao.update(film, id);
        } catch (EmptyResultDataAccessException e){
            throw new RequiredObjectWasNotFound("Film id " + id);
        }
    }

    public void like(int filmId, int userId) {
        filmDao.like(filmId, userId);
        log.info("User " + userId + " has liked film " + filmId);
    }


    public void deleteLike(int filmId, long userId) {
        int deletedRow = filmDao.deleteLike(filmId, userId);
        if(deletedRow == 0) {
            throw new RequiredObjectWasNotFound("Film id " + filmId + " User id " + userId);
        }
        log.info("User " + userId + " remove like from film " + filmId);
    }

    public List<Genre> getGenres() {
        return filmDao.getGenres();
    }

    public Genre getGenreById(int genreId) {
        try {

            return filmDao.getGenreById(genreId);
        } catch (EmptyResultDataAccessException e) {
            throw new RequiredObjectWasNotFound("Genre not found");
        }
    }

    public List<Mpa> getMpaRatings() {
        return filmDao.getMpaRatings();
    }

    public Mpa getMpaById(int mpaId) {
        if (mpaId > 5 || mpaId < 1) {
            throw new RequiredObjectWasNotFound("Not Valid mpaId");
        }
        return filmDao.getMpaById(mpaId);
    }
}
