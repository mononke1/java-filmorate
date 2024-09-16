package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Service
@Slf4j
public class UserService {
    private final UserStorage userStorage;

    public UserService(UserStorage userStorage) {
        this.userStorage = userStorage;
    }

    public User addFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new ValidationException("Пользователь не может добавить самого себя в друзья.");
        }

        User user = findById(userId);
        User friend = findById(friendId);

        addFriendIfNotPresent(user, friendId, userId);
        addFriendIfNotPresent(friend, userId, friendId);

        log.info("Пользователь с ID {} добавил в друзья пользователя с ID {}", userId, friendId);
        return user;
    }

    private void addFriendIfNotPresent(User user, Long friendId, Long userId) {
        if (!user.getFriends().add(friendId)) {
            throw new ValidationException("Пользователь с ID " + userId + " уже является другом пользователя с ID " + friendId);
        }
    }

    public User removeFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new ValidationException("Пользователь не может удалить самого себя из друзей.");
        }

        User user = findById(userId);
        User friend = findById(friendId);

        removeFriendIfPresent(user, friendId, userId);
        removeFriendIfPresent(friend, userId, friendId);

        log.info("Пользователь с ID {} удалил из друзей пользователя с ID {}", userId, friendId);
        return user;
    }

    private void removeFriendIfPresent(User user, Long friendId, Long userId) {
        if (!user.getFriends().remove(friendId)) {
            throw new ValidationException("Пользователь с ID " + userId + " не является другом пользователя с ID " + friendId);
        }
    }

    public Collection<Long> getCommonFriends(Long id, Long otherId) {
        User user = findById(id);
        User otherUser = findById(otherId);

        Set<Long> userFriends = user.getFriends();
        Set<Long> otherUserFriends = otherUser.getFriends();

        Set<Long> commonFriendIds = new HashSet<>(userFriends);
        commonFriendIds.retainAll(otherUserFriends);
        log.info("Найдено {} общих друзей между пользователями с ID {} и {}", commonFriendIds.size(), id, otherId);
        return commonFriendIds;
    }

    public Collection<Long> getFriends(Long id) {
        User user = findById(id);
        log.info("Пользователь с ID {} имеет {} друзей", id, user.getFriends().size());
        return user.getFriends();
    }

    public Collection<User> findAll() {
        return userStorage.findAll();
    }

    public User findById(Long id) {
        return userStorage.findById(id);
    }

    public User create(User user) {
        return userStorage.create(user);
    }

    public User update(User newUser) {
        return userStorage.update(newUser);
    }

    public User delete(Long id) {
        return userStorage.delete(id);
    }
}
