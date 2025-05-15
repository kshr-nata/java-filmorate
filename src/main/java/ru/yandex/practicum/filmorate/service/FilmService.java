package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.FilmDto;
import ru.yandex.practicum.filmorate.dto.NewFilmRequest;
import ru.yandex.practicum.filmorate.dto.UpdateFilmRequest;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.FilmMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.*;


import java.util.*;

@Slf4j
@Service
public class FilmService {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;
    private final RatingStorage ratingStorage;
    private final GenreStorage genreStorage;

    @Autowired
    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage,
                       RatingStorage ratingStorage,
                       GenreStorage genreStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.ratingStorage = ratingStorage;
        this.genreStorage = genreStorage;
    }

    public FilmService(@Qualifier("filmDbStorage") FilmStorage filmStorage,
                       @Qualifier("userDbStorage") UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.ratingStorage = null;
        this.genreStorage = null;
    }

    public Collection<FilmDto> findAll() {
        return filmStorage.findAll()
                .stream()
                .peek(film -> film.setGenres(new HashSet<>(genreStorage.findAllByFilmId(film.getId()))))
                .map(FilmMapper::mapToFilmDto)
                .toList();
    }

    public FilmDto create(NewFilmRequest request) {
        Film film = FilmMapper.mapToFilm(request);
        Rating rating = ratingStorage.findById(film.getMpa().getId()).orElseThrow(()
                -> new NotFoundException(String.format("MPA с id %d не найден",
                request.getMpa().getId())));
        Film finalFilm = filmStorage.create(film);
        if (request.hasGenres()) {
            request.getGenres().stream()
                    .map(genre -> genreStorage.findById(genre.getId())
                            .orElseThrow(() -> new NotFoundException(
                                    "Жанр с id " + genre.getId() + " не найден")))
                    .forEach(genre -> genreStorage.createFilmGenres(finalFilm.getId(), genre.getId()));
        }
        Set<Genre> genres = new LinkedHashSet<>(genreStorage.findAllByFilmId(finalFilm.getId()));  // Сохраняем порядок
        finalFilm.setGenres(genres);
        return FilmMapper.mapToFilmDto(finalFilm);
    }

    public FilmDto update(UpdateFilmRequest request) {
        Film film = filmStorage.findById(request.getId()).orElseThrow(()
                -> new NotFoundException(String.format("Фильм с id %d не найден",
                request.getId())));
        if (request.hasMpa()) {
            Rating rating = ratingStorage.findById(film.getMpa().getId()).orElseThrow(()
                    -> new NotFoundException(String.format("MPA с id %d не найден",
                    request.getMpa().getId())));
        }
        film = FilmMapper.updateFilmFields(film, request);
        Film finalFilm = filmStorage.update(film);
        if (request.hasGenres()) {
            Collection<Genre> newGenres = request.getGenres().stream()
                    .map(genre -> genreStorage.findById(genre.getId())
                            .orElseThrow(() -> new NotFoundException(
                                    "Жанр с id " + genre.getId() + " не найден"))).toList();
            genreStorage.deleteFilmGenres(finalFilm.getId());
            newGenres.forEach(genre -> genreStorage.createFilmGenres(finalFilm.getId(), genre.getId()));
        }
        finalFilm.setGenres(new HashSet<>(genreStorage.findAllByFilmId(finalFilm.getId())));
        return FilmMapper.mapToFilmDto(finalFilm);
    }

    public FilmDto findFilmById(Long id) {
        Film film = filmStorage.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("Фильм с id %d не найден", id)));
        film.setGenres(new HashSet<>(genreStorage.findAllByFilmId(film.getId())));
        return FilmMapper.mapToFilmDto(film);
    }

    public void addLikeByUser(Long filmId, Long userId) {
        log.debug("Вызван метод addLikeByUser filmId = {}, userId = {}", filmId, userId);
        Film film = filmStorage.findById(filmId).orElseThrow(() ->
                new NotFoundException("Фильм с id = " + filmId + " не найден"));
        User user = userStorage.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь с id = " + userId + " не найден"));
        filmStorage.addLikeByUser(filmId, userId);
    }

    public void deleteLikeByUser(Long filmId, Long userId) {
        log.debug("Вызван метод deleteLikeByUser filmId = {}, userId = {}", filmId, userId);
        Film film = filmStorage.findById(filmId).orElseThrow(() ->
                new NotFoundException("Фильм с id = " + filmId + " не найден"));
        User user = userStorage.findById(userId).orElseThrow(() ->
                new NotFoundException("Пользователь с id = " + userId + " не найден"));

        filmStorage.deleteLikeByUser(filmId, userId);
    }

    public Collection<FilmDto> getPopularFilms(int count) {
        log.debug("Вызван метод getPopularFilms count = {}", count);
        return filmStorage.getPopularFilms(count)
                .stream()
                .peek(film -> film.setGenres(new HashSet<>(genreStorage.findAllByFilmId(film.getId()))))
                .map(FilmMapper::mapToFilmDto)
                .toList();
    }
}
