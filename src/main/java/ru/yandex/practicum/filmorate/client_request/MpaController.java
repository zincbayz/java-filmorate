package ru.yandex.practicum.filmorate.client_request;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.film.Mpa;
import ru.yandex.practicum.filmorate.service.FilmService;

import javax.validation.Valid;
import javax.validation.constraints.Max;
import javax.validation.constraints.Min;
import javax.validation.constraints.Size;
import java.util.List;

@RestController
@RequestMapping("/mpa")
public class MpaController {

    private final FilmService filmService;
    @Autowired
    public MpaController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping("/{id}")
    public Mpa getMpaById(@PathVariable int id) {
        return filmService.getMpaById(id);
    }
    @GetMapping()
    public List<Mpa> getMPARatings() {
        return filmService.getMpaRatings();
    }
}
