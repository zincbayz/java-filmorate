package ru.yandex.practicum.filmorate.client_request;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.model.user.User;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.exception_handler.ValidationException;
import ru.yandex.practicum.filmorate.model.film.Film;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
@SpringBootTest
@AutoConfigureMockMvc
class FilmsControllerTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    FilmService filmService;
    @Test
    void shouldReturnAllFilms() {
        List<Film> allFilms = List.of(Film.builder()
                        .name("Kesha")
                        .description("1")
                        .releaseDate(LocalDate.of(2002,2,23))
                        .duration(10)
                        .build());
        when(filmService.getAllFilms()).thenReturn(allFilms);
        FilmsController filmsController = new FilmsController(filmService);
        assertEquals(allFilms, filmsController.getAllFilms());
    }

    @Test
    void shouldCreateFilm() throws Exception {
        Film film = Film.builder()
                .name("Kesha")
                .description("1")
                .releaseDate(LocalDate.of(2002,2,23))
                .duration(10)
                .build();
        mockMvc.perform(
                        post("/films")
                                .content(objectMapper.writeValueAsString(film))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk());
    }

    @Test
    void whenNotValidFilm_shouldReturnValidationException() throws Exception {
        Film film = Film.builder()
                .name("")
                .description("q")
                .releaseDate(LocalDate.of(2002,2,23))
                .duration(1)
                .build();
        when(filmService.create(film)).thenThrow(new ValidationException("Error"));
        mockMvc.perform(
                        post("/films").header("Content-Type", "application/json").content(objectMapper.writeValueAsString(film))
                ).andExpect(status().isBadRequest())
                .andExpect((mvcResult -> mvcResult.getResolvedException().getClass().equals(ValidationException.class)));

    }

    @Test
    void shouldCreateOk_afterTheUpdateFilm() throws Exception {
        Film film = Film.builder()
                .name("Kesha")
                .description("1")
                .releaseDate(LocalDate.of(2002,2,23))
                .duration(10)
                .build();
        when(filmService.update(film, 1)).thenReturn(film);
        mockMvc.perform(
                        put("/films")
                                .content(objectMapper.writeValueAsString(film))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk());
    }
    @Test
    void methodUpdateShouldReturnException() throws Exception {
        Film film = Film.builder()
                .name("Kesha")
                .description("1")
                .releaseDate(LocalDate.of(2002,2,23))
                .duration(-10)
                .build();
        when(filmService.update(film, 1)).thenThrow(ValidationException.class);
        mockMvc.perform(
                        put("/films")
                                .header("Content-Type", "application/json")
                                .content(objectMapper.writeValueAsString(film))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isBadRequest())
                .andExpect((mvcResult -> mvcResult.getResolvedException().getClass().equals(ValidationException.class)));
    }

    @Test
    void methodShouldOneFilm() throws Exception {
        Film film = Film.builder()
                .name("Kesha")
                .description("1")
                .releaseDate(LocalDate.of(2002,2,23))
                .duration(10)
                .build();
        when(filmService.getFilm(1)).thenReturn(film);

        mockMvc.perform(
                        get("/films/1")
                                .content(objectMapper.writeValueAsString(film))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk());
    }

    @Test
    void shouldReturnAllTopFilmsWithCountEqual2() {
        List<Film> allFilms = List.of(Film.builder()
                .name("Kesha")
                .description("1")
                .releaseDate(LocalDate.of(2002,2,23))
                .duration(10)
                .build(),

                Film.builder()
                        .name("Kesha2")
                        .description("12")
                        .releaseDate(LocalDate.of(2002,2,23))
                        .duration(100)
                        .build());
        when(filmService.getPopularFilms(2)).thenReturn(allFilms);
        FilmsController filmsController = new FilmsController(filmService);
        assertEquals(allFilms, filmsController.getPopularFilms(String.valueOf(2)));
    }
}
