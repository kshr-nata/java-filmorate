package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Rating;

import java.util.Collection;
import java.util.Optional;

@Slf4j
@Repository
@Qualifier("ratingDbStorage")
public class RatingDbStorage extends BaseRepository<Rating> implements RatingStorage {

    private static final String FIND_ALL_QUERY = "SELECT * FROM ratings";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM ratings WHERE id = ?";

    public RatingDbStorage(JdbcTemplate jdbc, RowMapper<Rating> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Collection<Rating> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public Optional<Rating> findById(Integer id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }

}
