package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.service.FilmService;
import ru.yandex.practicum.filmorate.storage.*;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;


@SpringBootTest
@AutoConfigureMockMvc
class FilmControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private FilmController controller;

    private NewFilmRequest validFilm;

    @BeforeEach
    void beforeEach() {
        validFilm = NewFilmRequest.builder()
                .name("Valid Film")
                .description("A valid film description")
                .releaseDate(LocalDate.of(1995, 12, 28)) // Корректная дата (после 28.12.1895)
                .duration(120)
                .build();
        Rating mpa = new Rating();
        mpa.setId(1);
        validFilm.setMpa(mpa);
        FilmStorage filmStorage = new InMemoryFilmStorage();
        UserStorage userStorage = new InMemoryUserStorage();
        FilmService filmService = new FilmService(filmStorage, userStorage);
        controller = new FilmController(filmService);
    }

    @SneakyThrows
    @Test
    void createFilm_ValidData_ReturnsOk() {
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Valid Film"));
    }

    @SneakyThrows
    @Test
    void createFilm_InvalidReleaseDate_ReturnsBadRequest() {
        Film invalidDateFilm = new Film();
        invalidDateFilm.setName("Invalid Date Film");
        invalidDateFilm.setDescription("Film with too early release date");
        invalidDateFilm.setReleaseDate(LocalDate.of(1890, 1, 1)); // Некорректная дата (раньше 28.12.1895)
        invalidDateFilm.setDuration(90);
        Rating mpa = new Rating();
        mpa.setId(1);
        invalidDateFilm.setMpa(mpa);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidDateFilm)))
                .andExpect(status().isBadRequest());
    }

    @SneakyThrows
    @Test
    void updateFilm_ValidData_ReturnsOk() {
        // Сначала создаем фильм
        String response = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andReturn().getResponse().getContentAsString();

        Film createdFilm = objectMapper.readValue(response, Film.class);
        createdFilm.setName("Updated Name");

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createdFilm)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @SneakyThrows
    @Test
    void updateFilm_InvalidReleaseDate_ReturnsBadRequest() {
        // Сначала создаем валидный фильм
        String response = mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andReturn().getResponse().getContentAsString();

        Film filmToUpdate = objectMapper.readValue(response, Film.class);
        filmToUpdate.setReleaseDate(LocalDate.of(1890, 1, 1)); // Устанавливаем невалидную дату

        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(filmToUpdate)))
                .andExpect(status().isBadRequest());
    }

    @SneakyThrows
    @Test
    void getAllFilms_ReturnsOk() {
        mockMvc.perform(post("/films")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validFilm)));

        mockMvc.perform(get("/films"))
                .andExpect(status().isOk());
    }

    @Test
    void getAllFilms_WhenNoFilmsAdded_ReturnsEmptyList() {
        Collection<FilmDto> films = controller.findAll();
        assertTrue(films.isEmpty(), "При инициализации список фильмов не пустой");
    }

    @SneakyThrows
    @Test
    void updateFilmById_WhenFilmNotFound_ThrowsNotFoundException() {
        validFilm.setId(999L);
        mockMvc.perform(put("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validFilm)))
                .andExpect(status().isNotFound())
                .andExpect(result -> {
                    // Проверяем, что было выброшено нужное исключение
                    assertInstanceOf(NotFoundException.class, result.getResolvedException());
                    assertEquals("Фильм с id 999 не найден",
                            result.getResolvedException().getMessage());
                });
    }

    @SneakyThrows
    @Test
    void createFilm_DescriptionTooLong_ReturnsBadRequest() {
        // Генерируем строку длиной 201 символ
        String longDescription = "a".repeat(201);

        Film invalidFilm = new Film();
        invalidFilm.setName("Film With Long Description");
        invalidFilm.setDescription(longDescription);  // longDescription - строка, превышающая допустимую длину
        invalidFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        invalidFilm.setDuration(120);
        Rating mpa = new Rating();
        mpa.setId(1);
        invalidFilm.setMpa(mpa);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidFilm)))
                .andExpect(status().isBadRequest());
    }

    @SneakyThrows
    @Test
    void createFilm_NegativeDuration_ReturnsBadRequest() {
        Film invalidFilm = new Film();
        invalidFilm.setName("Film With Negative Duration");
        invalidFilm.setDescription("Description");
        invalidFilm.setReleaseDate(LocalDate.of(2000, 1, 1));
        invalidFilm.setDuration(-10);
        Rating mpa = new Rating();
        mpa.setId(1);
        invalidFilm.setMpa(mpa);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidFilm)))
                .andExpect(status().isBadRequest());
    }

    @SneakyThrows
    @Test
    void createFilm_DurationExactly1Minute_ReturnsOk() {
        Film film = new Film();
        film.setName("One Minute Film");
        film.setDescription("Shortest possible film");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(1);  // Минимальная положительная длительность
        Rating mpa = new Rating();
        mpa.setId(1);
        film.setMpa(mpa);
        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.duration").value(1));
    }

    @SneakyThrows
    @Test
    void createFilm_ZeroDuration_ReturnsBadRequest() {
        Film film = new Film();
        film.setName("Zero Duration Film");
        film.setDescription("Normal description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(0);  // Установка нулевой длительности
        Rating mpa = new Rating();
        mpa.setId(1);
        film.setMpa(mpa);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest());
    }

    @SneakyThrows
    @Test
    void createFilm_DescriptionExactly200Chars_ReturnsOk() {
        String exact200Chars = "a".repeat(200); // Ровно 200 символов
        Film film = new Film();
        film.setName("Boundary Length Description");
        film.setDescription(exact200Chars);  // Предполагается, что exact200Chars = строка длиной 200 символов
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);
        Rating mpa = new Rating();
        mpa.setId(1);
        film.setMpa(mpa);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value(exact200Chars));
    }

    @SneakyThrows
    @Test
    void createFilm_ReleaseDateExactlyMinBoundary_ReturnsOk() {

        Film film = new Film();
        film.setName("Boundary Date Film");
        film.setDescription("Normal description");
        film.setReleaseDate(LocalDate.of(1895, 12, 28)); // Граничная дата (первый день кино)
        Rating mpa = new Rating();
        mpa.setId(1);
        film.setMpa(mpa);

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.releaseDate").value("1895-12-28"));
    }
}