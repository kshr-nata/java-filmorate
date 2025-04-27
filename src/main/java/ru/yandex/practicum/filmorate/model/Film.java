package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import ru.yandex.practicum.filmorate.validation.NotBeforeDate;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

/**
 * Film.
 */
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder(toBuilder = true)
public class Film {
    @EqualsAndHashCode.Include
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
    Set<Long> genres;
    Rating rating;

    public Set<Long> getLikes() {
        if (likes == null) {
            return new HashSet<>();
        }
        return likes;
    }

    public static Comparator<Film> byLikesCount() {
        return Comparator.comparingInt(film -> film.getLikes() != null ? film.getLikes().size() : 0);
    }

    public static Comparator<Film> byLikesCountDesc() {
        return byLikesCount().reversed();
    }

}
