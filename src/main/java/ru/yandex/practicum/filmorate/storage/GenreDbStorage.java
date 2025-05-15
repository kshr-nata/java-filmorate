package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.Collection;
import java.util.Optional;

@Slf4j
@Repository
public class GenreDbStorage extends BaseRepository<Genre> implements GenreStorage {

    private static final String FIND_ALL_QUERY = "SELECT * FROM genres";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM genres WHERE id = ?";
    private static final String FIND_ALL_BY_FILM_ID_QUERY = """
    SELECT g.id, g.name FROM genres g
    INNER JOIN film_genres fg ON g.id = fg.genre_id WHERE fg.film_id = ?
    ORDER BY
        g.id""";
    private static final String INSERT_INTO_FILM_GENRES_QUERY =
            "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
    private static final String DELETE_FROM_FILM_GENRES_QUERY =
            "DELETE FROM film_genres WHERE film_id = ?";

    public GenreDbStorage(JdbcTemplate jdbc, RowMapper<Genre> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Collection<Genre> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public Optional<Genre> findById(Integer id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }

    @Override
    public void createFilmGenres(long filmId, int genreId) {
        insert(INSERT_INTO_FILM_GENRES_QUERY,
                false,
                filmId,
                genreId);
    }

    @Override
    public void deleteFilmGenres(long filmId) {
        delete(DELETE_FROM_FILM_GENRES_QUERY, filmId);
    }

    @Override
    public Collection<Genre> findAllByFilmId(Long filmId) {
        return findMany(FIND_ALL_BY_FILM_ID_QUERY, filmId);
    }
}
