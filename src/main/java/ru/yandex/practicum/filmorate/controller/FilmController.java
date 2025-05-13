package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;


@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final FilmService filmService;

    @Autowired
    public FilmController(FilmService filmService) {
        this.filmService = filmService;
    }

    @GetMapping
    public Collection<FilmDto> findAll() {
        return filmService.findAll();
    }

    @PostMapping
    public FilmDto create(@Valid @RequestBody NewFilmRequest request) {
        return filmService.create(request);
    }

    @PutMapping
    public FilmDto update(@Valid @RequestBody UpdateFilmRequest request) {
        return filmService.update(request);
    }

    @GetMapping("/{id}")
    public FilmDto findFilmById(@PathVariable long id) {
       return filmService.findFilmById(id);
    }

    @PutMapping("{id}/like/{userId}")
    public void addLikeByUser(@PathVariable("id") long filmId, @PathVariable long userId) {
        filmService.addLikeByUser(filmId, userId);
    }

    @DeleteMapping("{id}/like/{userId}")
    public void deleteLikeByUser(@PathVariable("id") long filmId, @PathVariable long userId) {
        filmService.deleteLikeByUser(filmId, userId);
    }

    @GetMapping("popular")
    public Collection<FilmDto> getPopularFilms(@RequestParam(defaultValue = "10") int count) {
        if (count <= 0) {
            throw new ValidationException("Размер должен быть больше нуля");
        }
        return filmService.getPopularFilms(count);
    }
}
