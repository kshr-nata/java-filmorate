package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class FilmControllerTest {

    private FilmController controller;

    @BeforeEach
    void beforeEach() {
        controller = new FilmController();
    }


    @Test
    void createFilm_InvalidReleaseDate_ThrowsValidationException() {
        Film film = Film.builder()
                .name("Old Film")
                .releaseDate(LocalDate.of(1890, 1, 1))
                .build();

        assertThrows(ValidationException.class, () -> controller.create(film));
    }

    @Test
    void updateFilm_ValidId_UpdatesFilm() {
        Film film = Film.builder()
                .name("Film")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .build();
        Film createdFilm = controller.create(film);

        Film updatedFilm = createdFilm.toBuilder().name("Updated Name").build();
        Film result = controller.update(updatedFilm);

        assertEquals("Updated Name", result.getName());
    }

    @Test
    void findAll_EmptyList_ReturnsEmptyCollection() {
        Collection<Film> films = controller.findAll();
        assertTrue(films.isEmpty(), "Список фильмов должен быть пустым при инициализации");
    }

    @Test
    void findAll_NonEmptyList_ReturnsAllFilms() {
        final Film validFilm = Film.builder()
                .name("Valid Film")
                .description("Normal description")
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();
        controller.create(validFilm);
        Collection<Film> films = controller.findAll();
        assertEquals(1, films.size(), "Список фильмов должен содержать 1 фильм");
        assertTrue(films.contains(validFilm), "Список должен содержать созданный фильм");
    }

    @Test
    void updateFilm_NonExistentId_ThrowsNotFoundException() {
        Film nonExistentFilm = Film.builder()
                .id(999L)  // Несуществующий ID
                .name("Non-existent Film")
                .build();

        assertThrows(NotFoundException.class,
                () -> controller.update(nonExistentFilm),
                "При обновлении несуществующего фильма должно выбрасываться NotFoundException"
        );
    }

    @Test
    void createFilm_DescriptionExactly200Chars_Returns200() {
        final Film filmWithMaxDescription = Film.builder()
                .name("Film with max description")
                .description("A".repeat(200))  // Ровно 200 символов
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(90)
                .build();
        Film createdFilm = controller.create(filmWithMaxDescription);
        assertEquals(200, createdFilm.getDescription().length(),
                "Описание должно остаться длиной 200 символов");
    }

    //Тест для граничного значения: дата релиза ровно 28 декабря 1895 года
    @Test
    void createFilm_ReleaseDateExactlyMinDate_Returns200() {
        Film filmWithMinDate = Film.builder()
                .name("Min Date Film")
                .releaseDate(LocalDate.of(1895, 12, 28))  // Минимальная допустимая дата
                .build();

        Film createdFilm = controller.create(filmWithMinDate);
        assertEquals(LocalDate.of(1895, 12, 28), createdFilm.getReleaseDate(),
                "Дата релиза должна быть сохранена как 28 декабря 1895 года");
    }

    // Тест: описание больше 200 символов -> ValidationException
    @Test
    void createFilm_DescriptionOver200Chars_ThrowsValidationException() {
        Film invalidDescriptionFilm = Film.builder()
                .name("Invalid Description Film")
                .description("A".repeat(201)) // 201 символ!
                .releaseDate(LocalDate.of(2000, 1, 1))
                .duration(120)
                .build();
        assertThrows(ValidationException.class,
                () -> controller.create(invalidDescriptionFilm),
                "Описание длиной больше 200 символов должно вызывать ValidationException"
        );
    }
}