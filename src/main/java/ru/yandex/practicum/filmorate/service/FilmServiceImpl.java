package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.repository.FilmRepository;
import ru.yandex.practicum.filmorate.exception_handler.exceptions.RequiredObjectWasNotFound;
import ru.yandex.practicum.filmorate.model.film.Genre;
import ru.yandex.practicum.filmorate.model.film.Mpa;
import ru.yandex.practicum.filmorate.repository.UserRepository;
import ru.yandex.practicum.filmorate.util.enums.EventType;
import ru.yandex.practicum.filmorate.util.enums.Operation;
import java.util.List;

@Slf4j
@Service
public class FilmServiceImpl implements FilmService {
    private static final int DIRECTOR_OR_TITLE = 1;

    private static final int DEFAULT_COUNTER_VALUE = 10;
    private static final String DIRECTOR = "director";
    private static final String LIKES = "likes";
    private final FilmRepository filmRepository;
    private final DirectorService directorService;

    private static final String START_YEAR = "-01-01";
    private static final String END_YEAR = "-12-31";
    private static final int SORTING_IS_NOT_SELECTED = 0;

    private final UserRepository userRepository;

    @Autowired
    public FilmServiceImpl(FilmRepository filmRepository, DirectorService directorService, UserRepository userRepository) {
        this.filmRepository = filmRepository;
        this.directorService = directorService;
        this.userRepository = userRepository;
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
        List<Film> popularFilmsWithGenres = filmRepository.getPopularFilms(countTopFilms);

        if (popularFilmsWithGenres.size() < countTopFilms) {
            List<Film> additionalFilms = getAllFilms();
            popularFilmsWithGenres.removeAll(additionalFilms);
            if (countTopFilms == DEFAULT_COUNTER_VALUE) {
                popularFilmsWithGenres.addAll(additionalFilms);
            } else {
                for (int i = 0; i <= (countTopFilms - 1); i++) {
                    popularFilmsWithGenres.add(additionalFilms.get(i));
                }
            }
        }
        return popularFilmsWithGenres;
    }

    @Override
    public List<Film> getMostPopulars(int limit, int genreId, int year) {
        String startYear = year + START_YEAR;
        String endYear = year + END_YEAR;
        if (genreId != SORTING_IS_NOT_SELECTED & year == SORTING_IS_NOT_SELECTED) {
            return filmRepository.getMostPopularsByGenre(genreId, limit);
        } else if (genreId == SORTING_IS_NOT_SELECTED & year != SORTING_IS_NOT_SELECTED) {
            return filmRepository.getMostPopularsByYear(startYear, endYear, limit);
        } else {
            return filmRepository.getMostPopularsByYearAndGenre(genreId, startYear, endYear, limit);
        }
    }

    @Override
    public Film create(Film film) {
        int filmId = filmRepository.createFilm(buildFilm(film));
        return getFilm(filmId);
    }

    @Override
    public Film update(Film film, int id) {
        try {
            return filmRepository.update(buildFilm(film), id);
        } catch (EmptyResultDataAccessException e) {
            throw new RequiredObjectWasNotFound("Film id " + id);
        }
    }

    @Override
    public void deleteFilmById(int id) {
        filmRepository.deleteFilmById(id);
        log.info("Фильм с id {} удален", id);
    }

    @Override
    public void like(int filmId, int userId) {
        filmRepository.like(filmId, userId);
        userRepository.insertFeed(userId, EventType.LIKE, Operation.ADD, filmId);
        log.info("User " + userId + " has liked film " + filmId);
    }

    @Override
    public void deleteLike(int filmId, int userId) {
        int deletedRow = filmRepository.deleteLike(filmId, userId);
        if (deletedRow == 0) {
            throw new RequiredObjectWasNotFound("Film id " + filmId + " User id " + userId);
        }
        userRepository.insertFeed(userId, EventType.LIKE, Operation.REMOVE, filmId);
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

    @Override
    public List<Film> searchFilms() {
        return filmRepository.searchFilms();
    }

    @Override
    public List<Film> searchFilms(String query, List<String> by) {
        query = query.toLowerCase();
        if (by.size() == DIRECTOR_OR_TITLE) {
            if (DIRECTOR.equals(by.get(0))) {
                return filmRepository.searchFilmsByDirector(query);
            }
            return filmRepository.searchFilmsByTitle(query);
        }
        return filmRepository.searchFilmsByDirectorAndTitle(query);
    }

    @Override
    public List<Film> getCommonFilms(int userId, int friendId) {
        log.info("Список общих фильмов отправлен");
        return filmRepository.getCommonFilms(userId, friendId);
    }
    
    public List<Film> getSortedDirectorFilms(int directorId, String sortBy) {
        directorService.isDirectorExist(directorId);
        String sortRequest;
        if(LIKES.equals(sortBy)) {
            sortRequest = "SELECT *, (SELECT COUNT(user_id) FROM Likes GROUP BY film_id) AS likes FROM Films " +
                    "JOIN Mpa ON Films.mpa_id=Mpa.mpa_id " +
                    "JOIN Film_Director ON Films.film_id=Film_Director.film_id " +
                    "WHERE director_id = ? ORDER BY likes DESC";
        } else {
            sortRequest = "SELECT * FROM Films JOIN Mpa ON Films.mpa_id=Mpa.mpa_id JOIN Film_Director ON " +
                    "Films.film_id=Film_Director.film_id WHERE director_id = ? ORDER BY releaseDate";
        }
        return filmRepository.getSortedDirectorFilms(directorId, sortRequest);
    }


    private Film buildFilm(Film film) {
        return Film.builder()
                .id(film.getId())
                .name(film.getName())
                .description(film.getDescription())
                .releaseDate(film.getReleaseDate())
                .duration(film.getDuration())
                .mpa(film.getMpa())
                .genres(film.getGenres())
                .rate(film.getRate())
                .directors(film.getDirectors())
                .build();
    }
}
