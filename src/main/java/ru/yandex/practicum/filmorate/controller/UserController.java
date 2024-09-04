package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/users")
@Slf4j
public class UserController {

    private final Map<Long, User> users = new HashMap<>();

    @GetMapping
    public Collection<User> findAll() {
        log.info("Получен запрос на получение всех пользователей.");
        return users.values();
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        if (user == null) {
            log.error("Попытка создания пользователя с null значением.");
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

    @PutMapping
    public User update(@Valid @RequestBody User newUser) {
        if (newUser.getId() == null) {
            log.error("Ошибка при обновлении: ID пользователя не указан.");
            throw new ValidationException("Id должен быть указан.");
        }
        if (!users.containsKey(newUser.getId())) {
            log.error("Пользователь с ID {} не найден.", newUser.getId());
            throw new NotFoundException("Пользователь с id = " + newUser.getId() + " не найден.");
        }

        User existingUser = users.get(newUser.getId());
        log.info("Обновление пользователя с ID {}: {}", newUser.getId(), newUser);
        updateUserFields(existingUser, newUser);
        log.info("Пользователь с ID {} обновлен: {}", existingUser.getId(), existingUser);
        return existingUser;
    }

    private void updateUserFields(User existingUser, User newUser) {
        if (newUser.getLogin() != null) {
            log.debug("Обновление логина пользователя с ID {}: {}", existingUser.getId(), newUser.getLogin());
            existingUser.setLogin(newUser.getLogin());
        }
        if (!isNameInvalid(newUser)) {
            log.debug("Обновление имени пользователя с ID {}: {}", existingUser.getId(), newUser.getName());
            existingUser.setName(newUser.getName());
        } else {
            existingUser.setName(existingUser.getLogin());
            log.debug("Имя пользователя с ID {} установлено в логин: {}", existingUser.getId(), existingUser.getLogin());
        }
        if (newUser.getEmail() != null) {
            log.debug("Обновление email пользователя с ID {}: {}", existingUser.getId(), newUser.getEmail());
            existingUser.setEmail(newUser.getEmail());
        }
        if (newUser.getBirthday() != null) {
            log.debug("Обновление даты рождения пользователя с ID {}: {}", existingUser.getId(), newUser.getBirthday());
            existingUser.setBirthday(newUser.getBirthday());
        }
    }

    private boolean isNameInvalid(User user) {
        return user.getName() == null || user.getName().isBlank();
    }

    private long getNextId() {
        long currentMaxId = users.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        long newId = ++currentMaxId;
        log.debug("Сгенерирован новый ID: {}", newId);
        return newId;
    }
}
