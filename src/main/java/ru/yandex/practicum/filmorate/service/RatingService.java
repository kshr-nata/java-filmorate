package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.dto.RatingDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.RatingMapper;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.storage.RatingDbStorage;

import java.util.Collection;

@Slf4j
@Service
public class RatingService {

    private final RatingDbStorage ratingStorage;

    @Autowired
    public RatingService(RatingDbStorage ratingStorage) {
        this.ratingStorage = ratingStorage;
    }

    public Collection<RatingDto> findAll() {
        return ratingStorage.findAll()
                .stream()
                .map(RatingMapper::mapToRatingDto)
                .toList();
    }

    public RatingDto findById(Integer id) {
        Rating rating = ratingStorage.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("Mpa с id %d не найден", id)));
        return RatingMapper.mapToRatingDto(rating);
    }

}
