package ru.yandex.practicum.filmorate.storage.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
public class InMemoryUserStorage implements UserStorage {
    private final Map<Long, User> users = new HashMap<>();

    @Override
    public Collection<User> findAll() {
        log.info("Получен запрос на получение всех пользователей.");
        return users.values();
    }

    @Override
    public User create(User user) {
        if (user == null) {
            throw new ValidationException("Пользователь не может быть null.");
        }
        if (isNameInvalid(user)) {
            user.setName(user.getLogin());
            log.debug("Имя пользователя пустое, установлено значение логина: {}", user.getLogin());
        }
        user.setId(getNextId());
        users.put(user.getId(), user);
        log.info("Создан новый пользователь с ID {}: {}", user.getId(), user);
        return user;
    }

    @Override
    public User update(User newUser) {
        if (newUser.getId() == null) {
            throw new ValidationException("Ошибка при обновлении: ID пользователя не указан.");
        }
        if (!users.containsKey(newUser.getId())) {
            throw new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден.");
        }

        User existingUser = users.get(newUser.getId());
        log.info("Обновление пользователя с ID {}: {}", newUser.getId(), newUser);
        updateUserFields(existingUser, newUser);
        log.info("Пользователь с ID {} обновлен: {}", existingUser.getId(), existingUser);
        return existingUser;
    }

    @Override
    public User delete(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("Идентификатор пользователя не может быть null.");
        }
        User user = users.remove(userId);
        if (user == null) {
            throw new NotFoundException("Пользователь с ID " + userId + " не найден.");
        }
        log.info("Пользователь с ID {} был успешно удалён.", userId);
        return user;
    }

    @Override
    public User findById(Long id) {
        return Optional.ofNullable(users.get(id))
                .orElseThrow(() -> new NotFoundException("Пользователь с ID " + id + " не найден."));
    }

    private void updateUserFields(User existingUser, User newUser) {
        log.debug("Обновление логина пользователя с ID {}: {}", existingUser.getId(), newUser.getLogin());
        existingUser.setLogin(newUser.getLogin());

        if (!isNameInvalid(newUser)) {
            log.debug("Обновление имени пользователя с ID {}: {}", existingUser.getId(), newUser.getName());
            existingUser.setName(newUser.getName());
        } else {
            existingUser.setName(existingUser.getLogin());
            log.debug("Имя пользователя с ID {} установлено в логин: {}", existingUser.getId(), existingUser.getLogin());
        }
        if (newUser.getBirthday() != null) {
            log.debug("Обновление даты рождения пользователя с ID {}: {}", existingUser.getId(), newUser.getBirthday());
            existingUser.setBirthday(newUser.getBirthday());
        }

        log.debug("Обновление email пользователя с ID {}: {}", existingUser.getId(), newUser.getEmail());
        existingUser.setEmail(newUser.getEmail());
    }

    private boolean isNameInvalid(User user) {
        return user.getName() == null || user.getName().isBlank();
    }

    private long getNextId() {
        long currentMaxId = users.keySet().stream().mapToLong(id -> id).max().orElse(0);
        long newId = ++currentMaxId;
        log.debug("Сгенерирован новый ID: {}", newId);
        return newId;
    }
}
