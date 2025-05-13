package ru.yandex.practicum.filmorate.storage.mappers;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.RowMapper;
import ru.yandex.practicum.filmorate.model.User;

@Configuration
public class AppConfig {

    @Bean
    public RowMapper<User> userRowMapper() {
        return new UserRowMapper();
    }
}