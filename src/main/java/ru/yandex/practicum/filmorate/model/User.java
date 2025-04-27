package ru.yandex.practicum.filmorate.model;

import jakarta.validation.constraints.*;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@Builder(toBuilder = true)
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
