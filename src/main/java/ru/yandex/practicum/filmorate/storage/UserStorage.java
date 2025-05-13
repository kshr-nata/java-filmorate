package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.Optional;

public interface UserStorage {

    Collection<User> findAll();

    User create(User user);

    User update(User newUser);

    Optional<User> findById(Long id);

    Collection<User> getFriendsByUser(Long id);

    public Collection<User> getCommonFriends(Long id, Long otherId);

    public void addFriend(Long id, Long friendId);

    public void deleteFriend(Long id, Long friendId);

}
