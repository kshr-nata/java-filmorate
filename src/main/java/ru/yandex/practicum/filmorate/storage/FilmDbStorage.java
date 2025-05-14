package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.Optional;

@Slf4j
@Repository
@Qualifier("filmDbStorage")
public class FilmDbStorage extends BaseRepository<Film> implements FilmStorage {

    private static final String FIND_ALL_QUERY = """
    SELECT f.id, f.name, f.description, f.release_date, f.duration, f.rating_id, r.name AS mpa_name
    FROM films f INNER JOIN ratings r on f.rating_id = r.id""";
    private static final String FIND_BY_ID_QUERY = """
    SELECT f.id, f.name, f.description, f.release_date, f.duration, f.rating_id, r.name AS mpa_name
    FROM films f INNER JOIN ratings r on f.rating_id = r.id WHERE f.id = ?""";
    private static final String INSERT_QUERY = "INSERT INTO films(name, description, release_date, duration, rating_id)" +
            "VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_QUERY = """
    UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?,
    rating_id = ? WHERE id = ?""";
    private static final String INSERT_LIKE_QUERY = "INSERT INTO likes(film_id, user_id)" +
            "VALUES (?, ?)";
    private static final String DELETE_LIKE_QUERY
            = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
    private static final String FIND_POPULAR_QUERY = """
    SELECT f.id, f.name, f.description, f.release_date, f.duration, f.rating_id, r.name AS mpa_name,
    COUNT(l.user_id) AS likes_count
    FROM
        films f
    INNER JOIN ratings r ON f.rating_id = r.id
    LEFT JOIN
        likes l ON f.id = l.film_id
    GROUP BY
        f.id, r.name
    ORDER BY
        likes_count DESC
    LIMIT ?""";


    public FilmDbStorage(JdbcTemplate jdbc, RowMapper<Film> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Film create(Film film) {

        long id = insert(
                INSERT_QUERY,
                true,
                film.getName(),
                film.getDescription(),
                java.sql.Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId()
        );

        film.setId(id);
        return film;
    }

    @Override
    public Film update(Film film) {
        update(
                UPDATE_QUERY,
                film.getName(),
                film.getDescription(),
                java.sql.Date.valueOf(film.getReleaseDate()),
                film.getDuration(),
                film.getMpa().getId(),
                film.getId()
        );
        return film;
    }

    @Override
    public Collection<Film> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public Optional<Film> findById(Long id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }

    @Override
    public void addLikeByUser(Long filmId, Long userId) {
        long id = insert(
                INSERT_LIKE_QUERY,
                false,
                filmId,
                userId
        );
    }

    @Override
    public void deleteLikeByUser(Long filmId, Long userId) {
        delete(DELETE_LIKE_QUERY, filmId, userId);
    }

    @Override
    public Collection<Film> getPopularFilms(int count) {
        return findMany(FIND_POPULAR_QUERY, count);
    }
}
