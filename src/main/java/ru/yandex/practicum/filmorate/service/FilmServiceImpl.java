package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.film.Director;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.repository.FilmRepository;
import ru.yandex.practicum.filmorate.exception_handler.exceptions.RequiredObjectWasNotFound;
import ru.yandex.practicum.filmorate.model.film.Genre;
import ru.yandex.practicum.filmorate.model.film.Mpa;

import java.util.List;

@Slf4j
@Service
public class FilmServiceImpl implements FilmService {

    private final FilmRepository filmRepository;
    private final DirectorService directorService;

    @Autowired
    public FilmServiceImpl(@Qualifier("filmRepositoryImpl") FilmRepository filmRepository, DirectorService directorService) {
        this.filmRepository = filmRepository;
        this.directorService = directorService;
    }

    @Override
    public Film getFilm(int id) {
        try {
            return filmRepository.getFilm(id);
        } catch (EmptyResultDataAccessException e) {
            throw new RequiredObjectWasNotFound("Film " + id + " not found");
        }
    }

    @Override
    public List<Film> getAllFilms() {
        return filmRepository.getAllFilms();
    }

    @Override
    public List<Film> getPopularFilms(int countTopFilms) {
        return filmRepository.getPopularFilms(countTopFilms);
    }


    @Override
    public Film create(Film film) {
        Film createdFilm = filmRepository.createFilm(film);
        /*if(film.getDirector() != null) {
            return addDirectorToFilm(createdFilm, film.getDirector().getId());
        }*/
        return createdFilm;
    }

    private Film addDirectorToFilm(Film film, int directorId) {
        filmRepository.addDirectorToFilm(film.getId(), directorId);
        Director director = directorService.getDirectorById(directorId);
        film.setDirector(director);
        return film;
    }

    @Override
    public Film update(Film film, int id) {
        try {
            return filmRepository.update(film, id);
        } catch (EmptyResultDataAccessException e) {
            throw new RequiredObjectWasNotFound("Film id " + id);
        }
    }


    @Override
    public void like(int filmId, int userId) {
        filmRepository.like(filmId, userId);
        log.info("User " + userId + " has liked film " + filmId);
    }

    @Override
    public void deleteLike(int filmId, long userId) {
        int deletedRow = filmRepository.deleteLike(filmId, userId);
        if (deletedRow == 0) {
            throw new RequiredObjectWasNotFound("Film id " + filmId + " User id " + userId);
        }
        log.info("User " + userId + " remove like from film " + filmId);
    }

    @Override
    public List<Genre> getGenres() {
        return filmRepository.getGenres();
    }

    @Override
    public Genre getGenreById(int genreId) {
        try {

            return filmRepository.getGenreById(genreId);
        } catch (EmptyResultDataAccessException e) {
            throw new RequiredObjectWasNotFound("Genre not found");
        }
    }

    @Override
    public List<Mpa> getMpaRatings() {
        return filmRepository.getMpaRatings();
    }

    @Override
    public Mpa getMpaById(int mpaId) {
        if (mpaId > 5 || mpaId < 1) {
            throw new RequiredObjectWasNotFound("Not Valid mpaId");
        }
        return filmRepository.getMpaById(mpaId);
    }

    public List<Film> getSortedDirectorFilms(int directorId, String sortBy) {
        String sorting = "";
        if(sortBy == "year") {
            sorting = "ORDER BY COUNT(user_id) DESC";
        } else {
            sorting= "ORDER BY releaseDate DESC";
        }
        return filmRepository.getSortedDirectorFilms(directorId, sorting);
    }
}
