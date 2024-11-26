package ru.yandex.practicum.filmorate.storage.db;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.interfaces.LikeStorage;
import org.springframework.dao.DuplicateKeyException;

@Component("likeDbStorage")
@Slf4j
@RequiredArgsConstructor
public class LikeDbStorage implements LikeStorage {

    private final JdbcTemplate jdbc;
    private final FilmDbStorage filmDbStorage;

    @Override
    @Transactional
    public Film addLike(Long filmId, Long userId) {
        log.info("Добавление лайка от пользователя с ID {} к фильму с ID {}", userId, filmId);

        if (!existsFilmById(filmId)) {
            log.warn("Фильм с ID {} не найден", filmId);
            throw new NotFoundException("Фильм с ID " + filmId + " не найден.");
        }
        if (!existsUserById(userId)) {
            log.warn("Пользователь с ID {} не найден", userId);
            throw new NotFoundException("Пользователь с ID " + userId + " не найден.");
        }

        String insertLikeQuery = "INSERT INTO likes (film_id, user_id) VALUES (?, ?)";
        try {
            jdbc.update(insertLikeQuery, filmId, userId);
            log.debug("Лайк добавлен от пользователя с ID {} к фильму с ID {}", userId, filmId);
        } catch (DuplicateKeyException e) {
            log.warn("Лайк от пользователя с ID {} к фильму с ID {} уже существует", userId, filmId);
            throw new ValidationException("Лайк от пользователя с ID " + userId + " к фильму с ID " + filmId + " уже существует.");
        }

        return filmDbStorage.findById(filmId);
    }

    @Override
    @Transactional
    public Film removeLike(Long filmId, Long userId) {
        log.info("Удаление лайка от пользователя с ID {} к фильму с ID {}", userId, filmId);

        if (!existsFilmById(filmId)) {
            log.warn("Фильм с ID {} не найден", filmId);
            throw new NotFoundException("Фильм с ID " + filmId + " не найден.");
        }
        if (!existsUserById(userId)) {
            log.warn("Пользователь с ID {} не найден", userId);
            throw new NotFoundException("Пользователь с ID " + userId + " не найден.");
        }

        String deleteLikeQuery = "DELETE FROM likes WHERE film_id = ? AND user_id = ?";
        int rowsAffected = jdbc.update(deleteLikeQuery, filmId, userId);
        if (rowsAffected == 0) {
            log.warn("Лайк от пользователя с ID {} к фильму с ID {} не найден", userId, filmId);
            throw new NotFoundException("Лайк от пользователя с ID " + userId + " к фильму с ID " + filmId + " не найден.");
        }

        log.debug("Лайк удален от пользователя с ID {} к фильму с ID {}", userId, filmId);
        return filmDbStorage.findById(filmId);
    }

    private boolean existsFilmById(Long id) {
        String query = "SELECT EXISTS(SELECT 1 FROM films WHERE film_id = ?)";
        Boolean exists = jdbc.queryForObject(query, Boolean.class, id);
        return Boolean.TRUE.equals(exists);
    }

    private boolean existsUserById(Long id) {
        String query = "SELECT EXISTS(SELECT 1 FROM users WHERE user_id = ?)";
        Boolean exists = jdbc.queryForObject(query, Boolean.class, id);
        return Boolean.TRUE.equals(exists);
    }
}
