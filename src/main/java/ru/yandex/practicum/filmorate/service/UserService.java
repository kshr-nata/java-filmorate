package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.util.*;

@Slf4j
@Service
public class UserService {

    private final UserStorage userStorage;

    @Autowired
    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    public User create(User user) {
        return userStorage.create(user);
    }

    public User update(User newUser) {
        return userStorage.update(newUser);
    }

    public User findUserById(Long id) {
        log.debug("Вызван метод findUserById id = {}", id);
        return userStorage.findAll().stream()
                .filter(user -> user.getId().equals(id))
                .findFirst()
                .orElseThrow(() -> new NotFoundException(String.format("Пользователь с id %d не найден", id)));
    }

    public void addFriend(Long id, Long friendId) {
        log.debug("Вызван метод addFriend id = {}, friendId = {}", id, friendId);
        User user = findUserById(id);
        User friend = findUserById(friendId);

        // добавляем в друзья пользователя
        Set<Long> friends = user.getFriends();
        friends.add(friend.getId());
        user.setFriends(friends);

        // добавляем пользователя в друзья у соответствующего друга
        Set<Long> friendsOfFriend = friend.getFriends();
        friendsOfFriend.add(user.getId());
        friend.setFriends(friendsOfFriend);
    }

    public void deleteFriend(Long id, Long friendId) {
        log.debug("Вызван метод deleteFriend id = {}, friendId = {}", id, friendId);
        User user = findUserById(id);
        User friend = findUserById(friendId);

        // удаляем у пользователя
        Set<Long> friends = user.getFriends();
        friends.remove(friend.getId());
        user.setFriends(friends);

        // удаляем из друзей пользователя у соответствующего друга
        Set<Long> friendsOfFriend = friend.getFriends();
        friendsOfFriend.remove(user.getId());
        friend.setFriends(friendsOfFriend);
    }

    public Collection<User> getFriendsByUser(Long id) {
        log.debug("Вызван метод getFriendsByUser id = {}", id);
        User user = findUserById(id);
        return user.getFriends()
                .stream()
                .map(this::findUserById)
                .toList();
    }

    public Collection<User> getCommonFriends(Long id, Long otherId) {
        log.debug("Вызван метод getCommonFriends id = {}, otherId = {}", id, otherId);
        User user = findUserById(id);
        User otherUser = findUserById(otherId);
        return user.getFriends()
                .stream()
                .filter(otherUser.getFriends()::contains)
                .map(this::findUserById)
                .toList();
    }
}
