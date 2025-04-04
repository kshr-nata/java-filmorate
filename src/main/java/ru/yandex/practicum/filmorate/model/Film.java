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
}
