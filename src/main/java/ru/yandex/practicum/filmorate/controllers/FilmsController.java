package ru.yandex.practicum.filmorate.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception_handler.exeptions.EntityAllreadyExistExeption;
import ru.yandex.practicum.filmorate.exception_handler.exeptions.EntityNotFoundExeption;
import ru.yandex.practicum.filmorate.exception_handler.exeptions.InvalidParameterException;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.service.FilmServiceImpl;
import javax.validation.Valid;
import java.sql.SQLException;
import java.util.List;


@RestController
@RequestMapping("/films")
public class FilmsController {
    private final FilmServiceImpl filmServiceImpl;
    @Autowired
    public FilmsController(FilmServiceImpl filmServiceImpl) {
        this.filmServiceImpl = filmServiceImpl;
    }


    @GetMapping("/{id}")
    public Film getFilm(@PathVariable("id") int filmId) {
        return filmServiceImpl.getFilm(filmId);
    }

    @GetMapping
    public List<Film> getAllFilms() {
        return filmServiceImpl.getAllFilms();
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(
            @RequestParam(value = "count", defaultValue = "10", required = false) Integer count) throws InvalidParameterException {
        if (count <= 0) {
            throw new InvalidParameterException("count должен быть целым числом больше 0, получено " + count);
        }
        return filmServiceImpl.getPopularFilms(count);
    }

    @GetMapping("/director/{directorId}")
    public List<Film> getSortedDirectorFilms(@PathVariable int directorId, @RequestParam String sortBy) throws InvalidParameterException {
        if (sortBy.equals("year")) {
            return filmServiceImpl.getSortedDirectorFilmsByYear(directorId);

        }
        else if (sortBy.equals("likes")) {
            return filmServiceImpl.getSortedDirectorFilmsByLikes(directorId);
        }
        else {
            throw new InvalidParameterException("Неверно задан тип поиска ( " + sortBy + ")");
        }

    }


    @PostMapping
    public Film createFilm(@Valid @RequestBody Film film) throws SQLException {
        return filmServiceImpl.create(buildFilm(film));
    }

    @PutMapping()
    public Film updateFilm(@Valid @RequestBody Film film) {
        return filmServiceImpl.update(buildFilm(film), film.getId());
    }

    @PutMapping("/{id}/like/{userId}")
    public void like(@PathVariable("id") int filmId, @PathVariable int userId) throws EntityAllreadyExistExeption, EntityNotFoundExeption {
        filmServiceImpl.like(filmId, userId);
    }


    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable("id") int filmId, @PathVariable int userId) throws EntityNotFoundExeption {

        filmServiceImpl.deleteLike(filmId, userId);
    }

    private Film buildFilm(Film film) {
        return Film.builder()
                .id(film.getId())
                .name(film.getName())
                .description(film.getDescription())
                .releaseDate(film.getReleaseDate())
                .duration(film.getDuration())
                .rate(film.getRate())
                .mpa(film.getMpa())
                .genres(film.getGenres())
                .directors(film.getDirectors())
                .build();
    }

    @GetMapping("/search")
    private List<Film> searchFilms(
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "by", defaultValue = "", required = false) List<String> by) throws InvalidParameterException {

        if ( query==null || query.isBlank()) {
            return filmServiceImpl.searchFilms();
        }
        if ( by.size() == 1 && (by.get(0).equals("director") || by.get(0).equals("title")) ||
                (by.size() == 2 && (by.get(0).equals("director") && by.get(1).equals("title")))) {
            return filmServiceImpl.searchFilms(query, by);
        }
        else {
            throw new InvalidParameterException("Указаны неверные параметры запроса!");
        }
    }

}
