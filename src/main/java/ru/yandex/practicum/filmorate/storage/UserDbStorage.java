package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;

@Slf4j
@Repository
public class UserDbStorage extends BaseRepository<User> implements UserStorage {

    private static final String FIND_ALL_QUERY = "SELECT * FROM users";
    private static final String FIND_BY_ID_QUERY = "SELECT * FROM users WHERE id = ?";
    private static final String INSERT_QUERY = "INSERT INTO users(login, email, name, birthday)" +
            "VALUES (?, ?, ?, ?)";
    private static final String UPDATE_QUERY = """
    UPDATE users SET login = ?, email = ?, name = ?,
    birthday = ? WHERE id = ?""";
    private static final String FIND_ALL_USER_FRIENDS_QUERY = """
    SELECT * FROM users AS u
    INNER JOIN user_friends AS uf ON u.id = uf.friend_id WHERE uf.user_id = ? AND uf.confirmed = true""";
    private static final String FIND_COMMON_FRIEND_QUERY = """
    SELECT *
    FROM users u
    INNER JOIN user_friends uf1 ON uf1.friend_id = u.id
    INNER JOIN user_friends uf2 ON uf1.friend_id = uf2.friend_id
    WHERE uf1.user_id = ?
    AND uf2.user_id = ?
    """;
    private static final String INSERT_FRIEND_QUERY
            = "INSERT INTO user_friends (user_id, friend_id, confirmed) VALUES (?, ?, ?)";

    private static final String DELETE_FRIEND_QUERY
            = "DELETE FROM user_friends WHERE user_id = ? AND friend_id = ?";

    public UserDbStorage(JdbcTemplate jdbc, RowMapper<User> mapper) {
        super(jdbc, mapper);
    }

    @Override
    public Collection<User> findAll() {
        return findMany(FIND_ALL_QUERY);
    }

    @Override
    public User create(User user) {
        long id = insert(
                INSERT_QUERY,
                true,
                user.getLogin(),
                user.getEmail(),
                user.getName(),
                java.sql.Date.valueOf(user.getBirthday())
        );
        user.setId(id);
        return user;
    }

    @Override
    public User update(User user) {
        update(
                UPDATE_QUERY,
                user.getLogin(),
                user.getEmail(),
                user.getName(),
                java.sql.Date.valueOf(user.getBirthday()),
                user.getId()
        );
        return user;
    }

    @Override
    public Optional<User> findById(Long id) {
        return findOne(FIND_BY_ID_QUERY, id);
    }

    @Override
    public Collection<User> getFriendsByUser(Long id) {
        return findMany(FIND_ALL_USER_FRIENDS_QUERY, id);
    }

    @Override
    public Collection<User> getCommonFriends(Long id, Long otherId) {
        return findMany(FIND_COMMON_FRIEND_QUERY, id, otherId);
    }

    @Override
    public void addFriend(Long id, Long friendId) {
        insert(INSERT_FRIEND_QUERY,false, id, friendId, true);
        insert(INSERT_FRIEND_QUERY,false, friendId, id, false);
    }

    @Override
    public void deleteFriend(Long id, Long friendId) {
        delete(DELETE_FRIEND_QUERY, id, friendId);
    }
}
