package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class InMemoryGenreStorage implements GenreStorage {
    @Override
    public Collection<Genre> findAll() {
        return List.of();
    }

    @Override
    public Optional<Genre> findById(Integer id) {
        return Optional.empty();
    }

    @Override
    public void createFilmGenres(long filmId, int genreId) {

    }

    @Override
    public void deleteFilmGenres(long filmId) {

    }

    @Override
    public Collection<Genre> findAllByFilmId(Long filmId) {
        return List.of();
    }
}
