package ru.yandex.practicum.filmorate.storage.db;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.UserRowMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.interfaces.UserStorage;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component("userDbStorage")
@Slf4j
@RequiredArgsConstructor
public class UserDbStorage implements UserStorage {

    private final JdbcTemplate jdbc;
    private final UserRowMapper mapper;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public Collection<User> findAll() {
        log.info("Получен запрос на получение всех пользователей.");
        String query = "SELECT * FROM users";
        List<User> users = jdbc.query(query, mapper);
        setFriends(users);
        return users;
    }

    @Override
    public User findById(Long id) {
        log.info("Получен запрос на получение пользователя с ID {}", id);
        String query = "SELECT * FROM users WHERE user_id = ?";
        User user;
        try {
            user = jdbc.queryForObject(query, mapper, id);
        } catch (EmptyResultDataAccessException e) {
            log.warn("Пользователь с ID {} не найден", id);
            throw new NotFoundException("Пользователь с id=" + id + " не найден.");
        }
        setFriends(Collections.singletonList(user));
        return user;
    }

    @Override
    @Transactional
    public User create(User user) {
        log.info("Получен запрос на создание пользователя: {}", user);
        if (user == null) {
            log.error("Попытка создать пользователя с null значением");
            throw new ValidationException("Пользователь не может быть null.");
        }
        String INSERT_QUERY = "INSERT INTO users(user_name, login, email, birthday) VALUES (?, ?, ?, ?)";
        long id = insert(INSERT_QUERY,
                user.getName(),
                user.getLogin(),
                user.getEmail(),
                user.getBirthday());
        user.setId(id);
        insertFriends(user);
        log.info("Пользователь успешно создан: {}", user);
        return user;
    }

    @Override
    @Transactional
    public User update(User user) {
        log.info("Получен запрос на обновление пользователя с ID {}: {}", user.getId(), user);
        if (user.getId() == null) {
            log.error("ID пользователя не указан при попытке обновления");
            throw new ValidationException("Ошибка при обновлении: ID пользователя не указан.");
        }

        String UPDATE_QUERY = "UPDATE users SET user_name = ?, login = ?, email = ?, birthday = ? WHERE user_id = ?";
        int rowsUpdated = jdbc.update(
                UPDATE_QUERY,
                user.getName(),
                user.getLogin(),
                user.getEmail(),
                user.getBirthday(),
                user.getId()
        );

        if (rowsUpdated == 0) {
            log.warn("Пользователь с ID {} не найден для обновления", user.getId());
            throw new NotFoundException("Пользователь с id=" + user.getId() + " не найден.");
        }
        log.debug("Пользователь с ID {} обновлен", user.getId());

        jdbc.update("DELETE FROM friends WHERE user_id = ?", user.getId());
        insertFriends(user);

        log.info("Пользователь с ID {} успешно обновлен", user.getId());
        return user;
    }

    @Override
    @Transactional
    public User delete(Long userId) {
        log.info("Получен запрос на удаление пользователя с ID {}", userId);
        if (userId == null) {
            log.error("ID пользователя не может быть null при удалении");
            throw new IllegalArgumentException("Идентификатор пользователя не может быть null.");
        }

        User user = findById(userId);

        jdbc.update("DELETE FROM likes WHERE user_id = ?", userId);
        log.debug("Лайки пользователя с ID {} удалены", userId);
        jdbc.update("DELETE FROM friends WHERE user_id = ?", userId);
        log.debug("Друзья пользователя с ID {} удалены", userId);

        int rowsAffected = jdbc.update("DELETE FROM users WHERE user_id = ?", userId);

        if (rowsAffected == 0) {
            log.warn("Пользователь с ID {} не найден для удаления", userId);
            throw new NotFoundException("Пользователь с id=" + userId + " не найден.");
        }

        log.info("Пользователь с ID {} успешно удален", userId);
        return user;
    }

    private void insertFriends(User user) {
        if (user.getFriends() == null || user.getFriends().isEmpty()) {
            log.debug("Друзья не указаны для пользователя с ID {}, пропуск вставки друзей", user.getId());
            return;
        }

        String query = "INSERT INTO friends(user_id, friend_id, status) VALUES (?, ?, ?)";
        List<Object[]> batchArgs = user.getFriends().stream()
                .map(friendId -> new Object[]{user.getId(), friendId, true})
                .collect(Collectors.toList());
        jdbc.batchUpdate(query, batchArgs);
        log.debug("Друзья вставлены для пользователя с ID {}: {}", user.getId(), user.getFriends());
    }

    private long insert(String query, Object... params) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            for (int idx = 0; idx < params.length; idx++) {
                ps.setObject(idx + 1, params[idx]);
            }
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            log.debug("Запрос на вставку выполнен, получен ID: {}", key.longValue());
            return key.longValue();
        } else {
            log.error("Не удалось получить сгенерированный ключ после вставки");
            throw new InternalServerException("Не удалось сохранить данные");
        }
    }

    private void setFriends(List<User> users) {
        if (users.isEmpty()) {
            log.debug("Список пользователей пуст, установка друзей не требуется");
            return;
        }

        List<Long> userIds = users.stream()
                .map(User::getId)
                .collect(Collectors.toList());

        String sql = "SELECT user_id, friend_id FROM friends WHERE user_id IN (:userIds) AND status = true";

        Map<Long, User> userMap = users.stream()
                .collect(Collectors.toMap(User::getId, Function.identity()));

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("userIds", userIds);

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
        log.debug("Друзья установлены для пользователей: {}", userIds);
    }
}
