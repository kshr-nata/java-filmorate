package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Data
public class User {
    @EqualsAndHashCode.Include
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
    Map<Long, Boolean> friends;  //friendId, confirmed

    public Map<Long, Boolean> getFriends() {
        if (friends == null) {
            return new HashMap<Long, Boolean>();
        }
        return friends;
    }
}
