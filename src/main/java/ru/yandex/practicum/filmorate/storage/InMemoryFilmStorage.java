package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.*;

@Slf4j
@Component
public class InMemoryFilmStorage implements FilmStorage {

    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public Film create(Film film) {
        // формируем дополнительные данные
        film.setId(getNextId());
        // сохраняем нового пользователя в памяти приложения
        films.put(film.getId(), film);
        log.info("Создан фильм {}", film);
        return film;
    }

    @Override
    public Film update(Film newFilm) {
        // проверяем необходимые условия
        if (films.containsKey(newFilm.getId())) {
            Film oldFilm = films.get(newFilm.getId());
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

    @Override
    public Collection<Film> findAll() {
        return films.values();
    }

    @Override
    public Optional<Film> findById(Long id) {
        return Optional.ofNullable(films.get(id));
    }

    @Override
    public void addLikeByUser(Long filmId, Long userId) {
        Film film = findById(filmId).orElseThrow(() ->
                new NotFoundException("Фильм с id = " + filmId + " не найден"));
        Set<Long> likes = film.getLikes();
        likes.add(userId);
        film.setLikes(likes);
    }

    @Override
    public void deleteLikeByUser(Long filmId, Long userId) {
        Film film = findById(filmId).orElseThrow(() ->
                new NotFoundException("Фильм с id = " + filmId + " не найден"));
        Set<Long> likes = film.getLikes();
        likes.remove(userId);
        film.setLikes(likes);
    }

    @Override
    public Collection<Film> getPopularFilms(int count) {
        return findAll()
                .stream()
                .sorted(Film.byLikesCountDesc())
                .limit(count)
                .toList();
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
