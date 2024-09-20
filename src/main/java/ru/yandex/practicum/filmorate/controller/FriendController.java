package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.FriendService;

import java.util.Collection;

@RestController
@RequestMapping("/users/{userId}/friends")
@Slf4j
public class FriendController {

    private final FriendService friendService;

    @Autowired
    public FriendController(FriendService friendService) {
        this.friendService = friendService;
    }

    @PutMapping("/{friendId}")
    public User addFriend(@PathVariable Long userId, @PathVariable Long friendId) {
        log.info("Получен запрос на добавление в друзья пользователя с ID {} для пользователя с ID {}.", friendId, userId);
        return friendService.addFriend(userId, friendId);
    }

    @DeleteMapping("/{friendId}")
    public User removeFriend(@PathVariable Long userId, @PathVariable Long friendId) {
        log.info("Получен запрос на удаление из друзей пользователя с ID {} для пользователя с ID {}.", friendId, userId);
        return friendService.removeFriend(userId, friendId);
    }

    @GetMapping
    public Collection<User> getFriends(@PathVariable Long userId) {
        log.info("Получен запрос на получение списка друзей пользователя с ID {}.", userId);
        return friendService.getFriends(userId);
    }

    @GetMapping("/common/{otherId}")
    public Collection<User> getCommonFriends(@PathVariable Long userId, @PathVariable Long otherId) {
        log.info("Получен запрос на получение общих друзей между пользователем с ID {} и пользователем с ID {}.", userId, otherId);
        return friendService.getCommonFriends(userId, otherId);
    }
}
