package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Rating;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public class InMemoryRatingStorage implements RatingStorage {
    @Override
    public Collection<Rating> findAll() {
        return List.of();
    }

    @Override
    public Optional<Rating> findById(Integer id) {
        return Optional.empty();
    }
}
