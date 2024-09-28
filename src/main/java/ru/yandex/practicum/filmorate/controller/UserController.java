package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;

@RestController
@RequestMapping("/users")
@Slf4j
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping
    public Collection<User> findAll() {
        log.info("Получен запрос на получение всех пользователей.");
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public User findById(@PathVariable Long id) {
        log.info("Получен запрос на получение пользователя с ID {}.", id);
        return userService.findById(id);
    }

    @PostMapping
    public User create(@Valid @RequestBody User user) {
        log.info("Получен запрос на создание нового пользователя: {}", user);
        return userService.create(user);
    }

    @PutMapping
    public User update(@Valid @RequestBody User newUser) {
        log.info("Получен запрос на обновление пользователя с ID {}: {}", newUser.getId(), newUser);
        return userService.update(newUser);
    }

    @DeleteMapping("/{id}")
    public User delete(@PathVariable Long id) {
        log.info("Получен запрос на удаление пользователя с ID {}.", id);
        return userService.delete(id);
    }
}
