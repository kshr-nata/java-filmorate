package ru.yandex.practicum.filmorate;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.SneakyThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.dto.NewUserRequest;
import ru.yandex.practicum.filmorate.dto.UpdateUserRequest;
import ru.yandex.practicum.filmorate.dto.UserDto;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    private UserController controller;
    private NewUserRequest validUser;
    private UpdateUserRequest validUserUpdate;

    @BeforeEach
    void beforeEach() {
        UserStorage userStorage = new InMemoryUserStorage();
        UserService userService = new UserService(userStorage);
        controller = new UserController(userService);
        validUser = NewUserRequest.builder()
                .email("test@mail.ru")
                .login("validLogin")
                .name("Valid Name")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
        validUserUpdate = UpdateUserRequest.builder()
                .email("test@mail.ru")
                .login("validLogin")
                .name("Valid Name")
                .birthday(LocalDate.of(1990, 1, 1))
                .build();
    }

    @Test
    void findAll_ShouldReturnEmptyList_WhenNoUsersAdded() {
        Collection<UserDto> users = controller.findAll();
        assertTrue(users.isEmpty(), "Список пользователей должен быть пустым");
    }

    @Test
    void findAll_ShouldReturnAllUsers_WhenUsersExist() {
        controller.create(validUser);
        Collection<UserDto> users = controller.findAll();
        assertEquals(1, users.size(), "Должен вернуться 1 пользователь");
    }

    @SneakyThrows
    @Test
    void createUser_ValidData_ReturnsOk() {
        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.name").value("Valid Name"));
    }

    @SneakyThrows
    @Test
    void createUser_EmptyName_SetsNameAsLogin() {
        NewUserRequest user = validUser.toBuilder().name("").build();

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value(user.getLogin()));
    }

    @SneakyThrows
    @Test
    void createUser_InvalidEmail_ReturnsBadRequest() {
        NewUserRequest user = validUser.toBuilder().email("not-an-email").build();

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @SneakyThrows
    @Test
    void createUser_LoginWithSpaces_ReturnsBadRequest() {
        NewUserRequest user = validUser.toBuilder().login("login with spaces").build();

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @SneakyThrows
    @Test
    void createUser_FutureBirthday_ReturnsBadRequest() {
        NewUserRequest user = validUser.toBuilder()
                .birthday(LocalDate.now().plusDays(1))
                .build();

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
    }

    @SneakyThrows
    @Test
    void updateUser_ValidData_ReturnsOk() {
        // Сначала создаем пользователя
        String response = mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(validUser)))
                .andReturn().getResponse().getContentAsString();

        User createdUser = objectMapper.readValue(response, User.class);
        createdUser.setName("Updated Name");

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createdUser)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Name"));
    }

    @SneakyThrows
    @Test
    void updateUser_NonExistentId_ThrowsNotFoundException() {
        NewUserRequest user = validUser.toBuilder().id(999L).build();

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isNotFound())
                .andExpect(result -> assertInstanceOf(NotFoundException.class, result.getResolvedException()));
    }

    @SneakyThrows
    @Test
    void updateUser_MissingId_ThrowsValidationException() {
        UpdateUserRequest user = validUserUpdate.toBuilder().id(null).build();

        mockMvc.perform(put("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
      }

    @SneakyThrows
    @Test
    void getAllUsers_WithUsers_ReturnsUserList() {
        mockMvc.perform(post("/users")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(validUser)));

        mockMvc.perform(get("/users"))
                .andExpect(status().isOk());
    }

    @SneakyThrows
    @Test
    void createUser_EmptyLogin_ReturnsBadRequest() {
        NewUserRequest user = validUser.toBuilder().login("").build();

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isBadRequest());
      }

    @SneakyThrows
    @Test
    void createUser_BirthdayExactlyToday_ReturnsOk() {
        NewUserRequest user = validUser.toBuilder()
                .birthday(LocalDate.now())
                .build();

        mockMvc.perform(post("/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(user)))
                .andExpect(status().isOk());
    }

}