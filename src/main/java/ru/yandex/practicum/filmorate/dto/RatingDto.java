package ru.yandex.practicum.filmorate.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class RatingDto {
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    Integer id;
    String name;
}
