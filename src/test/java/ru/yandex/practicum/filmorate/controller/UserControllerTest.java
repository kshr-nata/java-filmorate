package ru.yandex.practicum.filmorate.controller;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTest {

    private UserController controller;
    private User validUser;

    @BeforeEach
    void beforeEach() {
        controller = new UserController();
        validUser = User.builder()
                .email("test@mail.ru")
                .login("validLogin")
                .name("Valid Name")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
    }

    @Test
    void findAll_ShouldReturnEmptyList_WhenNoUsersAdded() {
        Collection<User> users = controller.findAll();
        assertTrue(users.isEmpty(), "Список пользователей должен быть пустым");
    }

    @Test
    void findAll_ShouldReturnAllUsers_WhenUsersExist() {
        controller.create(validUser);
        Collection<User> users = controller.findAll();
        assertEquals(1, users.size(), "Должен вернуться 1 пользователь");
        assertTrue(users.contains(validUser), "Пользователь должен быть в списке");
    }

    @Test
    void create_ShouldSetLoginAsName_WhenNameIsEmpty() {
        User userWithEmptyName = User.builder()
                .email("test@mail.ru")
                .login("emptyNameUser")
                .name("")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
        User createdUser = controller.create(userWithEmptyName);
        assertEquals(userWithEmptyName.getLogin(), createdUser.getName(),
                "Если имя пустое, должен использоваться логин");
    }

    @Test
    void create_ShouldThrowValidationException_WhenLoginHasSpaces() {
        final User userWithSpacesInLogin = User.builder()
                .email("test@mail.ru")
                .login("invalid login")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
        assertThrows(ValidationException.class,
                () -> controller.create(userWithSpacesInLogin),
                "Логин с пробелами должен вызывать ValidationException"
        );
    }

    @Test
    void create_ShouldThrowValidationException_WhenBirthdayInFuture() {
        User userWithFutureBirthday = User.builder()
                .email("test@mail.ru")
                .login("futureBirthdayUser")
                .birthday(LocalDate.now().plusDays(1))
                .build();
        assertThrows(ValidationException.class,
                () -> controller.create(userWithFutureBirthday),
                "Дата рождения в будущем должна вызывать ValidationException"
        );
    }

    @Test
    void create_ShouldAssignId_WhenUserIsValid() {
        User createdUser = controller.create(validUser);
        assertNotNull(createdUser.getId(), "Пользователю должен быть присвоен ID");
    }

    // ======================================== PUT /users ========================================
    @Test
    void update_ShouldThrowValidationException_WhenIdIsNull() {
        User userWithoutId = User.builder().email("test@mail.ru").login("test").build();
        assertThrows(ValidationException.class,
                () -> controller.update(userWithoutId),
                "Обновление пользователя без ID должно вызывать ValidationException"
        );
    }

    @Test
    void update_ShouldThrowNotFoundException_WhenUserDoesNotExist() {
        User nonExistentUser = User.builder()
                .id(999L)
                .email("test@mail.ru")
                .login("test")
                .build();
        assertThrows(NotFoundException.class,
                () -> controller.update(nonExistentUser),
                "Обновление несуществующего пользователя должно вызывать NotFoundException"
        );
    }

    @Test
    void update_ShouldUpdateFields_WhenUserExists() {
        User originalUser = controller.create(validUser);
        User updatedUser = originalUser.toBuilder()
                .name("Updated Name")
                .email("updated@mail.ru")
                .build();

        User result = controller.update(updatedUser);
        assertEquals("Updated Name", result.getName(), "Имя должно обновиться");
        assertEquals("updated@mail.ru", result.getEmail(), "Email должен обновиться");
    }

    @Test
    void update_ShouldThrowValidationException_WhenUpdatedLoginHasSpaces() {
        User originalUser = controller.create(validUser);
        User invalidUser = originalUser.toBuilder().login("invalid login").build();

        assertThrows(ValidationException.class,
                () -> controller.update(invalidUser),
                "Обновление с логином, содержащим пробелы, должно вызывать ValidationException"
        );
    }

    @Test
    void create_ShouldAcceptBirthdayAsToday() {
        User userWithTodayBirthday = User.builder()
                .email("test@mail.ru")
                .login("todayBirthday")
                .birthday(LocalDate.now())
                .build();

        assertDoesNotThrow(
                () -> controller.create(userWithTodayBirthday),
                "Дата рождения сегодня должна быть допустима"
        );
    }

    @Test
    void create_ShouldGenerateIncrementalIds() {
        User firstUser = controller.create(validUser);
        User secondUser = controller.create(validUser.toBuilder().login("secondUser").build());

        assertEquals(firstUser.getId() + 1, secondUser.getId(),
                "ID должны инкрементироваться последовательно");
    }
}