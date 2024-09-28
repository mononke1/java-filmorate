package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FriendService;

import java.util.Collection;

@RestController
@RequestMapping("/users/{id}/friends")
@Slf4j
@RequiredArgsConstructor
public class FriendController {

    private final FriendService friendService;

    @PutMapping("/{friendId}")
    public User addFriend(@PathVariable Long id, @PathVariable Long friendId) {
        log.info("Получен запрос на добавление в друзья пользователя с ID {} для пользователя с ID {}.", friendId, id);
        return friendService.addFriend(id, friendId);
    }

    @DeleteMapping("/{friendId}")
    public User removeFriend(@PathVariable Long id, @PathVariable Long friendId) {
        log.info("Получен запрос на удаление из друзей пользователя с ID {} для пользователя с ID {}.", friendId, id);
        return friendService.removeFriend(id, friendId);
    }

    @GetMapping
    public Collection<User> getFriends(@PathVariable Long id) {
        log.info("Получен запрос на получение списка друзей пользователя с ID {}.", id);
        return friendService.getFriends(id);
    }

    @GetMapping("/common/{otherId}")
    public Collection<User> getCommonFriends(@PathVariable Long id, @PathVariable Long otherId) {
        log.info("Получен запрос на получение общих друзей между пользователем с ID {} и пользователем с ID {}.", id, otherId);
        return friendService.getCommonFriends(id, otherId);
    }
}
