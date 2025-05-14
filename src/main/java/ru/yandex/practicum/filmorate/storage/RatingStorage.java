package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Rating;

import java.util.Collection;
import java.util.Optional;

public interface RatingStorage {

    Collection<Rating> findAll();

    Optional<Rating> findById(Integer id);

}
