package ru.yandex.practicum.filmorate.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.service.FilmServiceImpl;
import javax.validation.Valid;
import ru.yandex.practicum.filmorate.exception_handler.exceptions.InvalidParameterException;

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

    @GetMapping("/common")
    public List<Film> getCommonFilms(@RequestParam int userId, @RequestParam int friendId) {
        return filmServiceImpl.getCommonFilms(userId, friendId);
    }
    
    @GetMapping("/director/{directorId}")
    public List<Film> getSortedDirectorFilms(@PathVariable int directorId, @RequestParam String sortBy) {
        return filmServiceImpl.getSortedDirectorFilms(directorId, sortBy);
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(@RequestParam(required = false, defaultValue = "10") String count,
                                      @RequestParam(required = false, defaultValue = "0") String genreId,
                                      @RequestParam(required = false, defaultValue = "0") String year) {

        if (Integer.parseInt(genreId) != 0 | Integer.parseInt(year) != 0) {
            return filmServiceImpl.getMostPopulars(Integer.parseInt(count),
                    Integer.parseInt(genreId), Integer.parseInt(year));
        } else return filmServiceImpl.getPopularFilms(Integer.parseInt(count));
    }

    @GetMapping("/search")
    private List<Film> searchFilms(
            @RequestParam(value = "query", required = false) String query,
            @RequestParam(value = "by", defaultValue = "", required = false) List<String> by) throws InvalidParameterException {

        query = query.toLowerCase();

        if ( query==null || query.isBlank()) {
            return filmServiceImpl.searchFilms();
        }
        if ( by.size() == 1 && (by.get(0).equals("director") || by.get(0).equals("title")) ||
                (by.size() == 2 && (by.get(0).equals("title") && by.get(1).equals("director"))) ||
                (by.size() == 2 && (by.get(0).equals("director") && by.get(1).equals("title")))) {
            return filmServiceImpl.searchFilms(query, by);
        }
        else {
            throw new InvalidParameterException("Указаны неверные параметры запроса!");
        }
    }

    @PostMapping
    public Film createFilm(@Valid @RequestBody Film film) {
        return filmServiceImpl.create(buildFilm(film));
    }

    @PutMapping()
    public Film updateFilm(@Valid @RequestBody Film film) {
        return filmServiceImpl.update(buildFilm(film), film.getId());
    }

    @PutMapping("/{id}/like/{userId}")
    public void like(@PathVariable("id") int filmId, @PathVariable int userId) {
        filmServiceImpl.like(filmId, userId);
    }


    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable("id") int filmId, @PathVariable int userId) {
        filmServiceImpl.deleteLike(filmId, userId);
    }

    @DeleteMapping("/{id}")
    public void deleteFilmById(@PathVariable int id) {
        filmServiceImpl.deleteFilmById(id);
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
                .directors(film.getDirectors())
                .build();
    }
}
