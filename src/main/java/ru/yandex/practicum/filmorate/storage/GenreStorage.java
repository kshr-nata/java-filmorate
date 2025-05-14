package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.Optional;

public interface GenreStorage {

    Collection<Genre> findAll();

    Optional<Genre> findById(Integer id);

    void createFilmGenres(long filmId, int genreId);

    void deleteFilmGenres(long filmId);

    Collection<Genre> findAllByFilmId(Long filmId);

}
