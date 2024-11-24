package ru.yandex.practicum.filmorate.storage.inmemory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.interfaces.LikeStorage;

@Slf4j
@Component
public class InMemoryLikeStorage implements LikeStorage {
    private final InMemoryFilmStorage filmService;
    private final InMemoryUserStorage userService;

    @Autowired
    public InMemoryLikeStorage(InMemoryFilmStorage filmService, InMemoryUserStorage userService) {
        this.filmService = filmService;
        this.userService = userService;
    }

    @Override
    public Film addLike(Long filmId, Long userId) {
        Film film = filmService.findById(filmId);
        userService.findById(userId);
        if (!film.getLikes().add(userId)) {
            throw new ValidationException("Пользователь с ID " + userId + " уже поставил лайк фильму с ID " + filmId);
        }
        log.info("Пользователь с ID {} добавил лайк фильму с ID {}", userId, filmId);
        return film;
    }

    @Override
    public Film removeLike(Long filmId, Long userId) {
        Film film = filmService.findById(filmId);
        userService.findById(userId);
        if (!film.getLikes().remove(userId)) {
            throw new NotFoundException("Пользователь с ID " + userId + " не ставил лайк фильму с ID " + filmId);
        }
        log.info("Пользователь с ID {} удалил лайк у фильма с ID {}", userId, filmId);
        return film;
    }
}
