package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.validation.NotBeforeDate;

import java.time.LocalDate;
import java.util.Set;

@Data
public class NewFilmRequest {
    Long id;
    @NotNull
    @NotBlank
    String name;
    @Size(max = 200, message = "Размер описания не должен превышать 200 символов")
    String description;
    @NotBeforeDate()
    LocalDate releaseDate;
    @Positive(message = "Длительность должна быть положительной")
    Integer duration;
    Set<Long> likes;
    Set<Genre> genres;
    Rating mpa;

    public boolean hasGenres() {
        return genres != null;
    }
}
