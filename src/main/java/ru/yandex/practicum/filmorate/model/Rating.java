package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
public class Rating {
    @EqualsAndHashCode.Include
    Integer id;
    @NotNull
    @NotBlank
    String name;
}
