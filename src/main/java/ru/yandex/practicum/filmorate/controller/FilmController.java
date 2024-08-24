package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/films")
@Slf4j
public class FilmController {
    private static final LocalDate MIN_DATE = LocalDate.of(1895, 12, 28);
    private final Map<Long, Film> films = new HashMap<>();

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Получен запрос на получение всех фильмов");
        return films.values();
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        if (film.getName().isBlank()) {
            log.error("Попытка создания фильма с пустым названием");
            throw new ValidationException("Название не может быть пустым");
        }
        validateDate(film);
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Создан новый фильм с ID {}: {}", film.getId(), film);
        return film;
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film newFilm) {
        if (newFilm.getId() == null) {
            log.error("Попытка обновления фильма без указания ID");
            throw new ValidationException("Id должен быть указан");
        }
        if (!films.containsKey(newFilm.getId())) {
            log.error("Фильм с id = {} не найден", newFilm.getId());
            throw new NotFoundException("Фильм с id = " + newFilm.getId() + " не найден");
        }

        Film existingFilm = films.get(newFilm.getId());
        updateFilmFields(existingFilm, newFilm);
        log.info("Фильм с ID {} был обновлен: {}", newFilm.getId(), existingFilm);
        return existingFilm;
    }

    private void validateDate(Film film) {
        if (film.getReleaseDate().isBefore(MIN_DATE)) {
            log.error("Дата релиза фильма с ID {} не соответствует минимальной дате: {}", film.getId(), MIN_DATE);
            throw new ValidationException("Дата релиза должна быть не раньше 28 декабря 1895 года");
        }
    }

    private void updateFilmFields(Film existingFilm, Film newFilm) {
        if (newFilm.getName() != null) {
            if (newFilm.getName().isBlank()) {
                log.error("Попытка обновления фильма с пустым названием");
                throw new ValidationException("Имя пользователя не должно быть пустым и не должно состоять только из пробелов");
            }
            log.debug("Обновление названия фильма с ID {} на {}", existingFilm.getId(), newFilm.getName());
            existingFilm.setName(newFilm.getName());
        }
        if (newFilm.getDescription() != null) {
            log.debug("Обновление описания фильма с ID {}", existingFilm.getId());
            existingFilm.setDescription(newFilm.getDescription());
        }
        if (newFilm.getReleaseDate() != null) {
            validateDate(newFilm);
            log.debug("Обновление даты релиза фильма с ID {} на {}", existingFilm.getId(), newFilm.getReleaseDate());
            existingFilm.setReleaseDate(newFilm.getReleaseDate());
        }
        if (newFilm.getDuration() != null) {
            log.debug("Обновление продолжительности фильма с ID {} на {}", existingFilm.getId(), newFilm.getDuration());
            existingFilm.setDuration(newFilm.getDuration());
        }
    }

    private long getNextId() {
        long currentMaxId = films.keySet()
                .stream()
                .mapToLong(id -> id)
                .max()
                .orElse(0);
        long newId = ++currentMaxId;
        log.debug("Сгенерирован новый ID для фильма: {}", newId);
        return newId;
    }
}
