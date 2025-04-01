package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.*;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {
    private final Map<Long, Film> films = new HashMap<>();
    private final LocalDate minReleaseDate = LocalDate.of(1895, 12, 28);

    @GetMapping
    public Collection<Film> findAll() {
        return films.values();
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        // проверяем выполнение необходимых условий
        if (film.getDescription() != null &&  film.getDescription().length() > 200) {
            log.warn("Ошибка при создании фильма {} : Максимальная длина описания — 200 символов!", film);
            throw new ValidationException("Максимальная длина описания — 200 символов!");
        }
        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(minReleaseDate)) {
            log.warn("Ошибка при создании фильма {} : Дата релиза — не раньше 28 декабря 1895 года!", film);
            throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года!");
        }
        // формируем дополнительные данные
        film.setId(getNextId());
        // сохраняем нового пользователя в памяти приложения
        films.put(film.getId(), film);
        log.info("Создан фильм {}", film);
        return film;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film newFilm) {
        // проверяем необходимые условия
        if (newFilm.getId() == null) {
            log.warn("Ошибка при обновлении фильма {}: Id должен быть указан", newFilm);
            throw new ValidationException("Id должен быть указан");
        }
        if (films.containsKey(newFilm.getId())) {
            Film oldFilm = films.get(newFilm.getId());
            if (newFilm.getDescription() != null && newFilm.getDescription().length() > 200) {
                log.warn("Ошибка при обновлении фильма {}: Максимальная длина описания — 200 символов!", newFilm);
                throw new ValidationException("Максимальная длина описания — 200 символов!");
            }
            if (newFilm.getReleaseDate() != null && newFilm.getReleaseDate().isBefore(minReleaseDate)) {
                log.warn("Ошибка при обновлении фильма {}: Дата релиза — не раньше 28 декабря 1895 года!", newFilm);
                throw new ValidationException("Дата релиза — не раньше 28 декабря 1895 года!");
            }
            if (newFilm.getReleaseDate() != null) {
                oldFilm.setReleaseDate(newFilm.getReleaseDate());
            }
            if (newFilm.getName() != null) {
                oldFilm.setName(newFilm.getName());
            }
            if (newFilm.getDuration() != null) {
                oldFilm.setDuration(newFilm.getDuration());
            }
            if (newFilm.getDescription() != null) {
                oldFilm.setDescription(newFilm.getDescription());
            }
            log.info("Фильм {} обновлен", oldFilm);
            return oldFilm;
        }
        log.warn("Фильм с id = {} не найден", newFilm.getId());
        throw new NotFoundException("Фильм с id = " + newFilm.getId() + " не найден");
    }

    // вспомогательный метод для генерации идентификатора нового пользователя
    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
