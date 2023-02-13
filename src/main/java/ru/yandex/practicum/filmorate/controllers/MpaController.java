package ru.yandex.practicum.filmorate.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.film.Mpa;
import ru.yandex.practicum.filmorate.service.FilmServiceImpl;

import java.util.List;

@RestController
@RequestMapping("/mpa")
public class MpaController {

    private final FilmServiceImpl filmServiceImpl;
    @Autowired
    public MpaController(FilmServiceImpl filmServiceImpl) {
        this.filmServiceImpl = filmServiceImpl;
    }

    @GetMapping("/{id}")
    public Mpa getMpaById(@PathVariable int id) {
        return filmServiceImpl.getMpaById(id);
    }
    @GetMapping()
    public List<Mpa> getMPARatings() {
        return filmServiceImpl.getMpaRatings();
    }
}
