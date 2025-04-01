package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {

    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAll() {
        return users.values();
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        // проверяем выполнение необходимых условий
        if (user.getLogin().trim().indexOf(" ") > 0) {
            log.warn("Ошибка при создании пользователя {}: Логин не может содержать пробелы!", user);
            throw new ValidationException("Логин не может содержать пробелы!");
        }
        if (user.getBirthday().isAfter(LocalDate.ofInstant(Instant.now(), ZoneId.systemDefault()))) {
            log.warn("Ошибка при создании пользователя {}: Дата рождения не может быть в будущем!", user);
            throw new ValidationException("Дата рождения не может быть в будущем!");
        }
        if (user.getName() == null || user.getName().isBlank()) {
           user.setName(user.getLogin());
        }
        // формируем дополнительные данные
        user.setId(getNextId());
        // сохраняем нового пользователя в памяти приложения
        users.put(user.getId(), user);
        log.info("Создан пользователь {}", user);
        return user;
    }

    @PutMapping
    public User update(@Valid @RequestBody User newUser) {
        // проверяем необходимые условия
        if (newUser.getId() == null) {
            log.warn("Ошибка при обновлении пользователя {}: Id должен быть указан", newUser);
            throw new ValidationException("Id должен быть указан");
        }
        if (users.containsKey(newUser.getId())) {
            User oldUser = users.get(newUser.getId());
            if (newUser.getLogin().trim().indexOf(" ") > 0) {
                log.warn("Ошибка при обновлении пользователя {}: Логин не может содержать пробелы!", newUser);
                throw new ValidationException("Логин не может содержать пробелы!");
            }
            if (newUser.getBirthday().isAfter(LocalDate.ofInstant(Instant.now(), ZoneId.systemDefault()))) {
                log.warn("Ошибка при обновлении пользователя {}: Дата рождения не может быть в будущем!", newUser);
                throw new ValidationException("Дата рождения не может быть в будущем!");
            }
            if (newUser.getBirthday() != null) {
                oldUser.setBirthday(newUser.getBirthday());
            }
            if (newUser.getEmail() != null) {
                oldUser.setEmail(newUser.getEmail());
            }
            if (newUser.getName() != null) {
                oldUser.setName(newUser.getName());
            }
            if (newUser.getLogin() != null) {
                oldUser.setLogin(newUser.getLogin());
            }
            log.info("Данные пользователя {} обновлены", oldUser);
            return oldUser;
        }
        log.warn("Пользователь с id = {} не найден", newUser.getId());
        throw new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден");
    }

    // вспомогательный метод для генерации идентификатора нового пользователя
    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
