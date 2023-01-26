package ru.yandex.practicum.filmorate.client_request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.model.film.Genre;
import ru.yandex.practicum.filmorate.model.film.Mpa;
import ru.yandex.practicum.filmorate.service.FilmService;
import javax.validation.Valid;
import java.util.List;


@RestController
@RequestMapping("/films")
public class FilmsController {
    private final FilmService filmService;
    @Autowired
    public FilmsController(FilmService filmService) {
        this.filmService = filmService;
    }


    @GetMapping("/{id}")
    public Film getFilm(@PathVariable("id") int filmId) {
        return filmService.getFilm(filmId);
    }

    @GetMapping
    public List<Film> getAllFilms() {
        return filmService.getAllFilms();
    }

    @GetMapping("/popular")
    public List<Film> getPopularFilms(@RequestParam(required = false, defaultValue = "10") String count) {
        return filmService.getPopularFilms(Integer.parseInt(count));
    }





    @PostMapping
    public Film createFilm(@Valid @RequestBody Film film) {
        return filmService.create(buildFilm(film));
    }

    @PutMapping()
    public Film updateFilm(@Valid @RequestBody Film film) {
        return filmService.update(buildFilm(film), film.getId());
    }

    @PutMapping("/{id}/like/{userId}")
    public void like(@PathVariable("id") int filmId, @PathVariable int userId) {
        filmService.like(filmId, userId);
    }


    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable("id") int filmId, @PathVariable int userId) {
        filmService.deleteLike(filmId, userId);
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
                .build();
    }
}
