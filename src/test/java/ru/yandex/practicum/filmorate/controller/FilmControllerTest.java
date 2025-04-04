package ru.yandex.practicum.filmorate.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

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

    private Film validFilm;

    @BeforeEach
    void beforeEach() {
        validFilm = Film.builder()
                .name("Valid Film")
                .description("A valid film description")
                .releaseDate(LocalDate.of(1995, 12, 28)) // Корректная дата (после 28.12.1895)
                .duration(120)
                .build();
        controller = new FilmController();
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
        Film invalidDateFilm = Film.builder()
                .name("Invalid Date Film")
                .description("Film with too early release date")
                .releaseDate(LocalDate.of(1890, 1, 1)) // Некорректная дата (раньше 28.12.1895)
                .duration(90)
                .build();
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
        Collection<Film> films = controller.findAll();
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
                    assertEquals("Фильм с id = 999 не найден",
                            result.getResolvedException().getMessage());
                });
    }

    @SneakyThrows
    @Test
    void createFilm_DescriptionTooLong_ReturnsBadRequest() {
        // Генерируем строку длиной 201 символ
        String longDescription = "a".repeat(201);

        Film invalidFilm = Film.builder()
                .name("Film With Long Description")
                .description(longDescription)
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidFilm)))
                .andExpect(status().isBadRequest());
    }

    @SneakyThrows
    @Test
    void createFilm_NegativeDuration_ReturnsBadRequest() {
        Film invalidFilm = Film.builder()
                .name("Film With Negative Duration")
                .description("Description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(-10)  // Отрицательное значение
                .build();

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidFilm)))
                .andExpect(status().isBadRequest());
    }

    @SneakyThrows
    @Test
    void createFilm_DurationExactly1Minute_ReturnsOk() {
        Film film = Film.builder()
                .name("One Minute Film")
                .description("Shortest possible film")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(1) // Минимальная положительная длительность
                .build();

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.duration").value(1));
    }

    @SneakyThrows
    @Test
    void createFilm_ZeroDuration_ReturnsBadRequest() {
        Film film = Film.builder()
                .name("Zero Duration Film")
                .description("Normal description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(0) // Нулевая длительность
                .build();

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isBadRequest());
    }

    @SneakyThrows
    @Test
    void createFilm_DescriptionExactly200Chars_ReturnsOk() {
        String exact200Chars = "a".repeat(200); // Ровно 200 символов

        Film film = Film.builder()
                .name("Boundary Length Description")
                .description(exact200Chars)
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.description").value(exact200Chars));
    }

    @SneakyThrows
    @Test
    void createFilm_ReleaseDateExactlyMinBoundary_ReturnsOk() {
        Film film = Film.builder()
                .name("Boundary Date Film")
                .description("Normal description")
                .releaseDate(LocalDate.of(1895, 12, 28)) // Граничная дата
                .duration(120)
                .build();

        mockMvc.perform(post("/films")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(film)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.releaseDate").value("1895-12-28"));
    }
}