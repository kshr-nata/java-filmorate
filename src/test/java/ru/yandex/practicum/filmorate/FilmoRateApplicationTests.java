package ru.yandex.practicum.filmorate;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Rating;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.*;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.RatingRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;

import java.time.LocalDate;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@RequiredArgsConstructor(onConstructor_ = @Autowired)
@Import({UserDbStorage.class, UserRowMapper.class, FilmDbStorage.class, FilmRowMapper.class,
RatingDbStorage.class, GenreDbStorage.class, RatingRowMapper.class, GenreRowMapper.class})
class FilmoRateApplicationTests {

    private final UserDbStorage userStorage;
    private final FilmDbStorage filmStorage;
    private final JdbcTemplate jdbcTemplate;
    private final RatingDbStorage ratingDbStorage;
    private final GenreDbStorage genreDbStorage;

    private User user;
    private User user2;
    private User user3;
    private Film film;
    private Film film2;

    @BeforeEach
    void setUp() {

        jdbcTemplate.update("DELETE FROM user_friends");
        jdbcTemplate.update("DELETE FROM users");
        jdbcTemplate.update("DELETE FROM likes");
        jdbcTemplate.update("ALTER TABLE users ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.update("DELETE FROM films");
        jdbcTemplate.update("ALTER TABLE films ALTER COLUMN id RESTART WITH 1");

        user = new User();
        user.setEmail("test@mail.ru");
        user.setLogin("login");
        user.setName("name");
        user.setBirthday(LocalDate.now());
        userStorage.create(user);

        user2 = new User();
        user2.setEmail("test2@mail.ru");
        user2.setLogin("login2");
        user2.setName("name2");
        user2.setBirthday(LocalDate.now());
        userStorage.create(user2);

        user3 = new User();
        user3.setEmail("test3@mail.ru");
        user3.setLogin("login3");
        user3.setName("name3");
        user3.setBirthday(LocalDate.now());
        userStorage.create(user3);

        film = new Film();
        film.setName("filmName");
        film.setDuration(100);
        film.setDescription("description");
        film.setReleaseDate(LocalDate.now());
        Rating mpa = new Rating();
        mpa.setId(1);
        film.setMpa(mpa);
        filmStorage.create(film);

        film2 = new Film();
        film2.setName("filmName2");
        film2.setDuration(200);
        film2.setDescription("description2");
        film2.setReleaseDate(LocalDate.now());
        Rating mpa2 = new Rating();
        mpa2.setId(2);
        film2.setMpa(mpa2);
        filmStorage.create(film2);

    }

    @Test
    public void testFindUserById() {

        Optional<User> userOptional = userStorage.findById(1L);

        assertThat(userOptional)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("id", 1L)
                );
    }

    @Test
    public void testFindAllUsers() {

        Collection<User> allUsers = userStorage.findAll();

        assertThat(allUsers.size()).isEqualTo(3);

        // Проверяем наличие всех email
        List<String> emails = allUsers.stream()
                .map(User::getEmail)
                .toList();
        assertThat(emails.contains("test@mail.ru")).isTrue();
    }

    @Test
    public void testUpdateUser() {

        user.setName("Updated name");
        userStorage.update(user);

        Collection<User> allUsers = userStorage.findAll();

        assertThat(allUsers.size()).isEqualTo(3);

        // Проверяем наличие всех email
        List<String> names = allUsers.stream()
                .map(User::getName)
                .toList();
        assertThat(names.contains("Updated name")).isTrue();
    }

    @Test
    public void testAddFriend() {
        userStorage.addFriend(1L, 2L);
        Collection<User> friends = userStorage.getFriendsByUser(1L);
        assertThat(friends.size()).isEqualTo(1);

        List<Long> ids = friends.stream()
                .map(User::getId)
                .toList();
        assertThat(ids.contains(2L)).isTrue();

        Collection<User> user2Friends = userStorage.getFriendsByUser(2L);
        assertThat(user2Friends.isEmpty()).isTrue();
    }

    @Test
    public void testDeleteFriend() {
        userStorage.addFriend(1L, 2L);
        Collection<User> friends = userStorage.getFriendsByUser(1L);
        assertThat(friends.size()).isEqualTo(1);

        List<Long> ids = friends.stream()
                .map(User::getId)
                .toList();
        assertThat(ids.contains(2L)).isTrue();

        Collection<User> user2Friends = userStorage.getFriendsByUser(2L);
        assertThat(user2Friends.isEmpty()).isTrue();
    }

    @Test
    public void testGetCommonFriends() {
        Collection<User> commonFriendsBefore = userStorage.getCommonFriends(2L, 3L);
        assertThat(commonFriendsBefore.isEmpty()).isTrue();

        userStorage.addFriend(2L, 1L);
        userStorage.addFriend(3L, 1L);

        Collection<User> commonFriendsAfter = userStorage.getCommonFriends(2L, 3L);
        assertThat(commonFriendsAfter.size()).isEqualTo(1);
        List<Long> ids = commonFriendsAfter.stream()
                .map(User::getId)
                .toList();
        assertThat(ids.contains(1L)).isTrue();
    }

    @Test
    public void testFindFilmById() {

        Optional<Film> filmOptional = filmStorage.findById(1L);

        assertThat(filmOptional)
                .isPresent()
                .hasValueSatisfying(user ->
                        assertThat(user).hasFieldOrPropertyWithValue("id", 1L)
                );
    }

    @Test
    public void testFindAllFilms() {

        Collection<Film> allFilms = filmStorage.findAll();

        assertThat(allFilms.size()).isEqualTo(2);

        List<String> names = allFilms.stream()
                .map(Film::getName)
                .toList();
        assertThat(names.contains("filmName")).isTrue();
    }

    @Test
    public void testUpdateFilm() {
        film.setName("Updated name");
        filmStorage.update(film);
        Collection<Film> allFilms = filmStorage.findAll();

        assertThat(allFilms.size()).isEqualTo(2);

        List<String> names = allFilms.stream()
                .map(Film::getName)
                .toList();
        assertThat(names.contains("Updated name")).isTrue();
    }

    @Test
    public void testAddLike() {
        filmStorage.addLikeByUser(2L, 1L);
        Collection<Film> mostLiked = filmStorage.getPopularFilms(1);
        assertThat(mostLiked.size()).isEqualTo(1);

        List<String> names = mostLiked.stream()
                .map(Film::getName)
                .toList();
        assertThat(names.contains("filmName2")).isTrue();
    }

    @Test
    public void testDeleteLike() {
        filmStorage.addLikeByUser(2L, 1L);
        String selectQuery = "SELECT * FROM likes";
        List<Map<String, Object>> rows = jdbcTemplate.queryForList(selectQuery);
        assertThat(rows.size()).isEqualTo(1);

        filmStorage.deleteLikeByUser(2L, 1L);
        List<Map<String, Object>> rowsAfter = jdbcTemplate.queryForList(selectQuery);
        assertThat(rowsAfter.isEmpty()).isTrue();
    }

    @Test
    public void testFindAllMpa() {
        Collection<Rating> allMpa = ratingDbStorage.findAll();
        assertThat(allMpa.size()).isEqualTo(5);
    }

    @Test
    public void testFindAllGenres() {
        Collection<Genre> allGenres = genreDbStorage.findAll();
        assertThat(allGenres.size()).isEqualTo(6);
    }

    @Test
    public void testFindMpaById() {
        Optional<Rating> optionalMpa = ratingDbStorage.findById(1);
        assertThat(optionalMpa)
                .isPresent()
                .hasValueSatisfying(mpa ->
                        assertThat(mpa).hasFieldOrPropertyWithValue("name", "G")
                );
    }

    @Test
    public void testFindGenreById() {
        Optional<Genre> optionalGenre = genreDbStorage.findById(1);
        assertThat(optionalGenre)
                .isPresent()
                .hasValueSatisfying(genre ->
                        assertThat(genre).hasFieldOrPropertyWithValue("name", "Комедия")
                );
    }

    @Test
    public void testFilmGenres() {
        Collection<Genre> filmGenresBeforeAdding = genreDbStorage.findAllByFilmId(1L);
        assertThat(filmGenresBeforeAdding.isEmpty()).isTrue();

        genreDbStorage.createFilmGenres(1L, 1);
        genreDbStorage.createFilmGenres(1L, 2);
        Collection<Genre> filmGenresAfterAdding = genreDbStorage.findAllByFilmId(1L);
        assertThat(filmGenresAfterAdding.size()).isEqualTo(2);

        genreDbStorage.deleteFilmGenres(1L);
        Collection<Genre> filmGenresAfterDeleting = genreDbStorage.findAllByFilmId(1L);
        assertThat(filmGenresAfterDeleting.isEmpty()).isTrue();

    }
}