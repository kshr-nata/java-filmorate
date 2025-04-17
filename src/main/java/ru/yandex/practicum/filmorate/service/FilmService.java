package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.FilmStorage;


import java.util.Collection;
import java.util.Set;

@Slf4j
@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserService userService;

    @Autowired
    public FilmService(FilmStorage filmStorage, UserService userService) {
        this.filmStorage = filmStorage;
        this.userService = userService;
    }

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film create(Film film) {
        return filmStorage.create(film);
    }

    public Film update(Film newFilm) {
        return filmStorage.update(newFilm);
    }

    public Film findFilmById(Long id) {
        return filmStorage.findAll().stream()
                .filter(film -> film.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(String.format("Фильм с id %d не найден", id)));
    }

    public void addLikeByUser(Long filmId, Long userId) {
        log.debug("Вызван метод addLikeByUser filmId = {}, userId = {}", filmId, userId);
        Film film = findFilmById(filmId);
        User user = userService.findUserById(userId);

        Set<Long> likes = film.getLikes();
        likes.add(user.getId());
        film.setLikes(likes);
    }

    public void deleteLikeByUser(Long filmId, Long userId) {
        log.debug("Вызван метод deleteLikeByUser filmId = {}, userId = {}", filmId, userId);
        Film film = findFilmById(filmId);
        User user = userService.findUserById(userId);

        Set<Long> likes = film.getLikes();
        likes.remove(user.getId());
        film.setLikes(likes);
    }

    public Collection<Film> getPopularFilms(int count) {
        log.debug("Вызван метод getPopularFilms count = {}", count);
        return filmStorage.findAll()
                .stream()
                .sorted(Film.byLikesCountDesc())
                .limit(count)
                .toList();
    }
}
