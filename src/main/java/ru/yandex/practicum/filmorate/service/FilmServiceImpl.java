package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception_handler.exeptions.EntityAllreadyExistExeption;
import ru.yandex.practicum.filmorate.exception_handler.exeptions.EntityNotFoundExeption;
import ru.yandex.practicum.filmorate.exception_handler.exeptions.RequiredObjectWasNotFound;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.repository.FilmRepository;
import ru.yandex.practicum.filmorate.model.film.Genre;
import ru.yandex.practicum.filmorate.model.film.Mpa;

import java.sql.SQLException;
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
    public Film create(Film film) throws SQLException {
        return filmRepository.createFilm(film);
    }

    @Override
    public Film update(Film film, int id) {
        try {
            return filmRepository.update(film, id);
        } catch (EmptyResultDataAccessException e) {
            throw new RequiredObjectWasNotFound("Film id " + id);
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void like(int filmId, int userId) throws EntityAllreadyExistExeption, EntityNotFoundExeption {
        if (!filmRepository.isUserExist(userId)) {
            throw new EntityNotFoundExeption("Нет пользователя c ID:" + userId);
        };
        if (!filmRepository.isFilmExist(filmId)) {
            throw new EntityNotFoundExeption("Нет фильма c ID:" + filmId);
        };
        if (filmRepository.isLikeExist(filmId, userId)) {
            throw new EntityAllreadyExistExeption("Пользователь с ID: " + filmId + " уже добавлял лайк фильму с ID: " + filmId);
        }
        filmRepository.like(filmId, userId);
        log.info("User " + userId + " has liked film " + filmId);
    }

    @Override
    public void deleteLike(int filmId, int userId) throws EntityNotFoundExeption {
        if (!filmRepository.isUserExist(userId)) {
            throw new EntityNotFoundExeption("Нет пользователя c ID:" + userId);
        };
        if (!filmRepository.isFilmExist(filmId)) {
            throw new EntityNotFoundExeption("Нет фильма c ID:" + filmId);
        };
        int deletedRow = filmRepository.deleteLike(filmId, userId);
        if (deletedRow == 0) {
            throw new EntityNotFoundExeption("Нет лайка фильма с id: " + filmId + " от пользователя с id: " + userId);
        }
        log.info("User " + userId + " remove like from film " + filmId);
    }

    @Override
    public List<Genre> getGenres() {
        return filmRepository.getGenres();
    }

    @Override
    public Genre getGenreById(int genreId) throws EntityNotFoundExeption {
        try {
            return filmRepository.getGenreById(genreId);
        } catch (EmptyResultDataAccessException e) {
            throw new EntityNotFoundExeption("Жанр с id: " + genreId + " не найден.");
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

    @Override
    public List<Film> searchFilms() {
        return filmRepository.searchFilms();
    }

    @Override
    public List<Film> searchFilms(String query, List<String> by) {

        if (by.size() == 1) {
            if (by.get(0).equals("director")) {
                return filmRepository.searchFilmsByDirector(query);
            }
            if (by.get(0).equals("title")) {
                return filmRepository.searchFilmsByTitle(query);
            }
        }
        return filmRepository.searchFilmsByDirectorAndTitle(query);
    }

    @Override
    public List<Film> getSortedDirectorFilmsByYear(int directorId) throws EntityNotFoundExeption {
        if (!filmRepository.isDirectorExist(directorId)) {
            throw new EntityNotFoundExeption("Нет режиссера c ID:" + directorId);
        };
        return filmRepository.getSortedDirectorFilmsByYear(directorId);
    }

    @Override
    public List<Film> getSortedDirectorFilmsByLikes(int directorId) throws EntityNotFoundExeption {
        if (!filmRepository.isDirectorExist(directorId)) {
            throw new EntityNotFoundExeption("Нет режиссера c ID:" + directorId);
        };
        return filmRepository.getSortedDirectorFilmsByLikes(directorId);
    }

    public List<Film> getSortedDirectorFilms(int directorId, String sortBy) {
        directorService.isDirectorExist(directorId);
        String sortRequest;
        if("likes".equals(sortBy)) {
            sortRequest = "SELECT * FROM Films f " +
                    "JOIN Mpa m ON f.mpa_id = m.mpa_id " +
                    "JOIN directors d ON f.film_id = d.director_id " +
                    "WHERE director_id = 1 " +
                    "ORDER BY rate";
        } else {
            sortRequest = "SELECT * FROM Films f " +
                    "JOIN Mpa m ON f.mpa_id = m.mpa_id " +
                    "JOIN directors d ON f.film_id = d.director_id " +
                    "WHERE director_id = 1 " +
                    "ORDER BY releaseDate";
        }
        return filmRepository.getSortedDirectorFilms(directorId, sortRequest);
    }


}
