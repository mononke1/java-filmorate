package ru.yandex.practicum.filmorate.storage.db;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.UserRowMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.interfaces.FriendStorage;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component("friendDbStorage")
@Slf4j
@RequiredArgsConstructor
public class FriendDbStorage implements FriendStorage {
    private final JdbcTemplate jdbc;
    private final UserDbStorage userDbStorage;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    @Transactional
    public User addFriend(Long userId, Long friendId) {
        log.info("Пользователь {} добавляет пользователя {} в друзья (односторонняя дружба)", userId, friendId);

        if (!userExists(userId)) {
            log.warn("Пользователь с ID {} не найден", userId);
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }
        if (!userExists(friendId)) {
            log.warn("Пользователь с ID {} не найден", friendId);
            throw new NotFoundException("Пользователь с ID " + friendId + " не найден");
        }

        if (userId.equals(friendId)) {
            log.warn("Пользователь {} не может добавить самого себя в друзья", userId);
            throw new ValidationException("Нельзя добавить самого себя в друзья");
        }

        String checkExistsSql = "SELECT COUNT(*) FROM friends WHERE user_id = ? AND friend_id = ?";
        Integer count = jdbc.queryForObject(checkExistsSql, Integer.class, userId, friendId);
        if (count != null && count > 0) {
            log.warn("Пользователь {} уже добавил пользователя {} в друзья", userId, friendId);
            throw new ValidationException("Пользователь уже добавлен в друзья");
        }

        String insertSql = "INSERT INTO friends (user_id, friend_id, status) VALUES (?, ?, ?)";
        jdbc.update(insertSql, userId, friendId, true);

        log.info("Пользователь {} успешно добавил пользователя {} в друзья", userId, friendId);
        return userDbStorage.findById(userId);
    }

    @Override
    @Transactional
    public User removeFriend(Long userId, Long friendId) {
        log.info("Пользователь {} удаляет пользователя {} из друзей (односторонняя дружба)", userId, friendId);

        if (!userExists(userId)) {
            log.warn("Пользователь с ID {} не найден", userId);
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }
        if (!userExists(friendId)) {
            log.warn("Пользователь с ID {} не найден", friendId);
            throw new NotFoundException("Пользователь с ID " + friendId + " не найден");
        }

        String deleteSql = "DELETE FROM friends WHERE user_id = ? AND friend_id = ?";
        jdbc.update(deleteSql, userId, friendId);

        log.info("Пользователь {} успешно удалил пользователя {} из друзей (если дружба существовала)", userId, friendId);
        return userDbStorage.findById(userId);
    }

    @Override
    public Collection<User> getCommonFriends(Long userId, Long otherUserId) {
        log.info("Получение общих друзей между пользователями {} и {}", userId, otherUserId);

        if (!userExists(userId)) {
            log.warn("Пользователь с ID {} не найден", userId);
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }
        if (!userExists(otherUserId)) {
            log.warn("Пользователь с ID {} не найден", otherUserId);
            throw new NotFoundException("Пользователь с ID " + otherUserId + " не найден");
        }

        String sql = "SELECT u.* FROM users u " +
                "JOIN friends f1 ON u.user_id = f1.friend_id " +
                "JOIN friends f2 ON u.user_id = f2.friend_id " +
                "WHERE f1.user_id = ? AND f2.user_id = ?";

        List<User> commonFriends = jdbc.query(sql, new UserRowMapper(), userId, otherUserId);

        setFriendsForUsers(commonFriends);

        return commonFriends;
    }

    @Override
    public Collection<User> getFriends(Long userId) {
        log.info("Получение списка друзей пользователя {}", userId);

        if (!userExists(userId)) {
            log.warn("Пользователь с ID {} не найден", userId);
            throw new NotFoundException("Пользователь с ID " + userId + " не найден");
        }

        String sql = "SELECT u.* FROM users u " +
                "JOIN friends f ON u.user_id = f.friend_id " +
                "WHERE f.user_id = ?";

        List<User> friends = jdbc.query(sql, new UserRowMapper(), userId);

        setFriendsForUsers(friends);

        return friends;
    }

    private void setFriendsForUsers(List<User> users) {
        if (users.isEmpty()) {
            return;
        }

        List<Long> userIds = users.stream().map(User::getId).collect(Collectors.toList());

        String sql = "SELECT f.user_id, f.friend_id FROM friends f WHERE f.user_id IN (:userIds)";

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("userIds", userIds);

        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        namedParameterJdbcTemplate.query(sql, parameters, rs -> {
            Long userId = rs.getLong("user_id");
            Long friendId = rs.getLong("friend_id");

            User user = userMap.get(userId);
            if (user != null) {
                if (user.getFriends() == null) {
                    user.setFriends(new HashSet<>());
                }
                user.getFriends().add(friendId);
            }
        });
    }

    private boolean userExists(Long userId) {
        String sql = "SELECT EXISTS(SELECT 1 FROM users WHERE user_id = ?)";
        Boolean exists = jdbc.queryForObject(sql, Boolean.class, userId);
        return Boolean.TRUE.equals(exists);
    }
}
