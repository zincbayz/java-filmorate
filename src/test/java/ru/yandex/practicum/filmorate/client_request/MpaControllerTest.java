package ru.yandex.practicum.filmorate.client_request;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.Util.FilmsMPARatings;
import ru.yandex.practicum.filmorate.model.film.Genre;
import ru.yandex.practicum.filmorate.model.film.Mpa;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MpaControllerTest {
    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MockMvc mockMvc;
    @MockBean
    FilmService filmService;

    @Test
    void getMpaById() throws Exception {
        Mpa mpa = new Mpa();
        when(filmService.getMpaById(1)).thenReturn(mpa);

        mockMvc.perform(
                        get("/mpa/1")
                                .content(objectMapper.writeValueAsString(mpa))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk());
    }

    @Test
    void getMPARatings() throws Exception {
        List<Mpa> allMpa = List.of(new Mpa(), new Mpa());

        when(filmService.getMpaRatings()).thenReturn(allMpa);

        mockMvc.perform(
                        get("/mpa")
                                .content(objectMapper.writeValueAsString(allMpa))
                                .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(status().isOk());
    }
}