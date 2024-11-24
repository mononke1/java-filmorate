
package ru.yandex.practicum.filmorate.storage.inmemory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.interfaces.FriendStorage;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Component
@Primary
public class InMemoryFriendStorage implements FriendStorage {

    private final InMemoryUserStorage userStorage;

    @Autowired
    public InMemoryFriendStorage(InMemoryUserStorage userStorage) {
        this.userStorage = userStorage;
    }

    @Override
    public User addFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new ValidationException("Пользователь не может добавить самого себя в друзья.");
        }

        User user = findById(userId);
        User friend = findById(friendId);

        addFriendIfNotPresent(user, friendId);
        addFriendIfNotPresent(friend, userId);

        log.info("Пользователь с ID {} добавил в друзья пользователя с ID {}", userId, friendId);
        return user;
    }

    private void addFriendIfNotPresent(User user, Long friendId) {
        if (!user.getFriends().contains(friendId)) {
            user.getFriends().add(friendId);
        } else {
            throw new ValidationException("Пользователь с ID " + user.getId() + " уже является другом пользователя с ID " + friendId);
        }
    }

    @Override
    public User removeFriend(Long userId, Long friendId) {
        if (userId.equals(friendId)) {
            throw new ValidationException("Пользователь не может удалить самого себя из друзей.");
        }

        User user = findById(userId);
        User friend = findById(friendId);

        removeFriendIfPresent(user, friendId);
        removeFriendIfPresent(friend, userId);

        log.info("Пользователь с ID {} удалил из друзей пользователя с ID {}", userId, friendId);
        return user;
    }

    private void removeFriendIfPresent(User user, Long friendId) {
        boolean removed = user.getFriends().remove(friendId);

        if (removed) {
            log.info("Пользователь с ID {} удалил друга с ID {}", user.getId(), friendId);
        } else {
            log.warn("Попытка удаления несуществующего друга. Пользователь с ID {} не является другом пользователя с ID {}", user.getId(), friendId);
        }
    }

    @Override
    public Collection<User> getCommonFriends(Long userId, Long otherUserId) {
        User user = findById(userId);
        User otherUser = findById(otherUserId);

        Set<Long> userFriends = user.getFriends();
        Set<Long> otherUserFriends = otherUser.getFriends();

        Set<Long> commonFriendIds = new HashSet<>(userFriends);
        commonFriendIds.retainAll(otherUserFriends);

        Collection<User> commonFriends = commonFriendIds.stream()
                .map(this::findById)
                .collect(Collectors.toList());

        log.info("Найдено {} общих друзей между пользователями с ID {} и {}", commonFriends.size(), userId, otherUserId);
        return commonFriends;
    }

    @Override
    public Collection<User> getFriends(Long userId) {
        User user = findById(userId);
        log.info("Пользователь с ID {} имеет {} друзей", userId, user.getFriends().size());
        return user.getFriends().stream()
                .map(this::findById)
                .collect(Collectors.toList());
    }

    private User findById(Long id) {
        User user = userStorage.findById(id);
        if (user == null) {
            throw new NotFoundException("Пользователь с ID " + id + " не найден.");
        }
        return user;
    }
}

