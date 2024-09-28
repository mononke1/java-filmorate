package ru.yandex.practicum.filmorate.storage.film;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.util.DateUtil;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Component
@Slf4j
public class InMemoryFilmStorage implements FilmStorage {
    private final Map<Long, Film> films = new HashMap<>();

    @Override
    public Collection<Film> findAll() {
        log.info("Получен запрос на получение всех фильмов.");
        return films.values();
    }

    @Override
    public Film create(Film film) {
        if (film == null) {
            throw new ValidationException("Фильм не может быть null.");
        }
        isValidateDate(film);
        film.setId(getNextId());
        films.put(film.getId(), film);
        log.info("Создан новый фильм с ID {}: {}", film.getId(), film);
        return film;
    }

    @Override
    public Film update(Film newFilm) {
        if (newFilm.getId() == null) {
            throw new ValidationException("Id должен быть указан.");
        }
        if (!films.containsKey(newFilm.getId())) {
            throw new NotFoundException("Фильм с id = " + newFilm.getId() + " не найден.");
        }

        Film existingFilm = films.get(newFilm.getId());
        try {
            updateFilmFields(existingFilm, newFilm);
        } catch (ValidationException e) {
            throw new ValidationException("Ошибка обновления", e);
        }
        log.info("Фильм с ID {} был обновлен: {}", newFilm.getId(), existingFilm);
        return existingFilm;
    }

    @Override
    public Film delete(Long filmId) {
        if (filmId == null) {
            throw new IllegalArgumentException("Идентификатор фильма не может быть null.");
        }
        Film film = films.remove(filmId);
        if (film == null) {
            throw new NotFoundException("Фильм с ID " + filmId + " не найден.");
        }
        log.info("Фильм с ID {} был успешно удалён.", filmId);
        return film;
    }

    @Override
    public Film findById(Long id) {
        return Optional.ofNullable(films.get(id))
                .orElseThrow(() -> new NotFoundException("Фильм с ID " + id + " не найден."));
    }

    private void updateFilmFields(Film existingFilm, Film newFilm) {
        log.debug("Обновление названия фильма с ID {} на {}", existingFilm.getId(), newFilm.getName());
        existingFilm.setName(newFilm.getName());

        if (newFilm.getDescription() != null && !newFilm.getDescription().isBlank()) {
            log.debug("Обновление описания фильма с ID {}", existingFilm.getId());
            existingFilm.setDescription(newFilm.getDescription());
        }
        if (isValidateDate(newFilm)) {
            log.debug("Обновление даты релиза фильма с ID {} на {}", existingFilm.getId(), newFilm.getReleaseDate());
            existingFilm.setReleaseDate(newFilm.getReleaseDate());
        }
        if (newFilm.getDuration() != null) {
            log.debug("Обновление продолжительности фильма с ID {} на {}", existingFilm.getId(), newFilm.getDuration());
            existingFilm.setDuration(newFilm.getDuration());
        }
    }

    private Boolean isValidateDate(Film film) {
        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(DateUtil.MIN_DATE)) {
            throw new ValidationException("Дата релиза должна быть не раньше 28 декабря 1895 года.");
        }
        return film.getReleaseDate() != null;
    }

    private long getNextId() {
        long currentMaxId = films.keySet().stream().mapToLong(id -> id).max().orElse(0);
        long newId = ++currentMaxId;
        log.debug("Сгенерирован новый ID для фильма: {}", newId);
        return newId;
    }
}
