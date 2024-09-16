package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.film.FilmStorage;
import ru.yandex.practicum.filmorate.storage.user.UserStorage;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
@Slf4j
public class FilmService {
    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    public FilmService(FilmStorage filmStorage, UserStorage userStorage) {
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
    }

    public Film addLike(Long filmId, Long userId) {
        Film film = findById(filmId);
        userStorage.findById(userId);
        if (!film.getLikes().add(userId)) {
            throw new ValidationException("Пользователь с ID " + userId + " уже поставил лайк фильму с ID " + filmId);
        }
        log.info("Пользователь с ID {} добавил лайк фильму с ID {}", userId, filmId);
        return film;
    }

    public Collection<Film> getTopFilms(int limit) {
        return findAll().stream()
                .sorted((f1, f2) -> Integer.compare(f2.getLikes().size(), f1.getLikes().size()))
                .limit(limit)
                .collect(Collectors.toList());
    }

    public Film removeLike(Long filmId, Long userId) {
        Film film = findById(filmId);
        userStorage.findById(userId);
        if (!film.getLikes().remove(userId)) {
            throw new NotFoundException("Пользователь с ID " + userId + " не ставил лайк фильму с ID " + filmId);
        }
        log.info("Пользователь с ID {} удалил лайк у фильма с ID {}", userId, filmId);
        return film;
    }

    public Collection<Film> findAll() {
        return filmStorage.findAll();
    }

    public Film create(Film film) {
        return filmStorage.create(film);
    }

    public Film update(Film newFilm) {
        return filmStorage.update(newFilm);
    }

    public Film delete(Long id) {
        return filmStorage.delete(id);
    }

    public Film findById(Long id) {
        return filmStorage.findById(id);
    }
}
