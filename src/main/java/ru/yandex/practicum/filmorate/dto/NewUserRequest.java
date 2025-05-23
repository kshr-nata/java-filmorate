package ru.yandex.practicum.filmorate.dto;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDate;

@Data
@Builder(toBuilder = true)
public class NewUserRequest {
    Long id;
    @Email
    @NotNull
    @NotBlank
    String email;
    @NotNull
    @NotBlank
    @Pattern(regexp = "\\S+", message = "Логин не должен содержать пробелов")
    String login;
    String name;
    @PastOrPresent
    LocalDate birthday;
}
