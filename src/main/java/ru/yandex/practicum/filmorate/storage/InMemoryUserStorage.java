package ru.yandex.practicum.filmorate.storage;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;

@Slf4j
@Component
@Qualifier("inMemoryUserStorage")
public class InMemoryUserStorage implements UserStorage {

    private final Map<Long, User> users = new HashMap<>();

    @Override
    public Collection<User> findAll() {
        return users.values();
    }

    @Override
    public User create(User user) {
        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }
        // формируем дополнительные данные
        user.setId(getNextId());
        // сохраняем нового пользователя в памяти приложения
        users.put(user.getId(), user);
        log.info("Создан пользователь {}", user);
        return user;
    }

    @Override
    public User update(User newUser) {
        // проверяем необходимые условия
        if (newUser.getId() == null) {
            log.warn("Ошибка при обновлении пользователя {}: Id должен быть указан", newUser);
            throw new ValidationException("Id должен быть указан");
        }
        if (users.containsKey(newUser.getId())) {
            User oldUser = users.get(newUser.getId());
            if (newUser.getBirthday() != null) {
                oldUser.setBirthday(newUser.getBirthday());
            }
            if (newUser.getEmail() != null) {
                oldUser.setEmail(newUser.getEmail());
            }
            if (newUser.getName() != null) {
                oldUser.setName(newUser.getName());
            }
            if (newUser.getLogin() != null) {
                oldUser.setLogin(newUser.getLogin());
            }
            // заменяем имя на логин, если имя пустое
            if (oldUser.getName().isBlank()) {
                oldUser.setName(oldUser.getLogin());
            }
            log.info("Данные пользователя {} обновлены", oldUser);
            return oldUser;
        }
        log.warn("Пользователь с id = {} не найден", newUser.getId());
        throw new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден");
    }

    @Override
    public Optional<User> findById(Long id) {
        return Optional.ofNullable(users.get(id));
    }

    @Override
    public Collection<User> getFriendsByUser(Long id) {
        User user = findById(id).orElseThrow(() -> new NotFoundException("Пользователь с id = " + id + " не найден"));
        return user.getFriends().keySet()
                .stream()
                .map(this::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    @Override
    public Collection<User> getCommonFriends(Long id, Long otherId) {
        User user = findById(id).orElseThrow(() ->
                new NotFoundException("Пользователь с id = " + id + " не найден"));
        User otherUser = findById(otherId).orElseThrow(() ->
                new NotFoundException("Пользователь с id = " + otherId + " не найден"));

        return user.getFriends().keySet()
                .stream()
                .filter(otherUser.getFriends().keySet()::contains)
                .map(this::findById)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .toList();
    }

    @Override
    public void addFriend(Long id, Long friendId) {
        User user = findById(id).orElseThrow(() ->
                new NotFoundException("Пользователь с id = " + id + " не найден"));
        User friend = findById(friendId).orElseThrow(() ->
                new NotFoundException("Пользователь с id = " + friendId + " не найден"));
        // добавляем в друзья пользователя
        Map<Long, Boolean> friends = user.getFriends();
        friends.put(friend.getId(), true);
        user.setFriends(friends);

        // добавляем пользователя в друзья у соответствующего друга
        Map<Long, Boolean> friendsOfFriend = friend.getFriends();
        friendsOfFriend.put(user.getId(), true);
        friend.setFriends(friendsOfFriend);
    }

    @Override
    public void deleteFriend(Long id, Long friendId) {
        User user = findById(id).orElseThrow(() ->
                new NotFoundException("Пользователь с id = " + id + " не найден"));
        User friend = findById(friendId).orElseThrow(() ->
                new NotFoundException("Пользователь с id = " + friendId + " не найден"));

        // удаляем у пользователя
        Map<Long, Boolean> friends = user.getFriends();
        friends.remove(friend.getId());
        user.setFriends(friends);

        // удаляем из друзей пользователя у соответствующего друга
        Map<Long, Boolean> friendsOfFriend = friend.getFriends();
        friendsOfFriend.remove(user.getId());
        friend.setFriends(friendsOfFriend);

    }

    // вспомогательный метод для генерации идентификатора нового пользователя
    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        return ++currentMaxId;
    }
}
