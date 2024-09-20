package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.LikeService;

@RestController
@RequestMapping("/films/{filmId}/likes")
@Slf4j
public class LikeController {

    private final LikeService likeService;

    @Autowired
    public LikeController(LikeService likeService) {
        this.likeService = likeService;
    }

    @PutMapping("/{userId}")
    public Film addLike(@PathVariable Long filmId, @PathVariable Long userId) {
        log.info("Получен запрос на добавление лайка пользователем с ID {} для фильма с ID {}.", userId, filmId);
        return likeService.addLike(filmId, userId);
    }

    @DeleteMapping("/{userId}")
    public Film removeLike(@PathVariable Long filmId, @PathVariable Long userId) {
        log.info("Получен запрос на удаление лайка пользователем с ID {} для фильма с ID {}.", userId, filmId);
        return likeService.removeLike(filmId, userId);
    }
}
