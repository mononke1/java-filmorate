package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.LikeService;

@RestController
@RequestMapping("/films/{id}/like")
@Slf4j
@RequiredArgsConstructor
public class LikeController {

    private final LikeService likeService;

    @PutMapping("/{userId}")
    public Film addLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Получен запрос на добавление лайка пользователем с ID {} для фильма с ID {}.", userId, id);
        return likeService.addLike(id, userId);
    }

    @DeleteMapping("/{userId}")
    public Film removeLike(@PathVariable Long id, @PathVariable Long userId) {
        log.info("Получен запрос на удаление лайка пользователем с ID {} для фильма с ID {}.", userId, id);
        return likeService.removeLike(id, userId);
    }
}
