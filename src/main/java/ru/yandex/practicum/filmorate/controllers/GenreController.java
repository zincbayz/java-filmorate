package ru.yandex.practicum.filmorate.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.exception_handler.exeptions.EntityNotFoundExeption;
import ru.yandex.practicum.filmorate.model.film.Genre;
import ru.yandex.practicum.filmorate.service.FilmServiceImpl;

import java.util.List;

@RestController
@RequestMapping("/genres")
public class GenreController {
    private final FilmServiceImpl filmServiceImpl;
    @Autowired
    public GenreController(FilmServiceImpl filmServiceImpl) {
        this.filmServiceImpl = filmServiceImpl;
    }

    @GetMapping("/{id}")
    public Genre getGenreById(@PathVariable("id") int genreId) throws EntityNotFoundExeption {
        return filmServiceImpl.getGenreById(genreId);
    }

    @GetMapping()
    public List<Genre> getGenres() {
        return filmServiceImpl.getGenres();
    }


}
