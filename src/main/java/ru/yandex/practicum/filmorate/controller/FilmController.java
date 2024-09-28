package ru.yandex.practicum.filmorate.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;

import java.util.Collection;

@RestController
@RequestMapping("/films")
@Slf4j
@RequiredArgsConstructor
public class FilmController {
    private final FilmService filmService;

    @GetMapping
    public Collection<Film> findAll() {
        log.info("Получен запрос на получение всех фильмов.");
        return filmService.findAll();
    }

    @GetMapping("/{id}")
    public Film findById(@PathVariable Long id) {
        log.info("Получен запрос на получение фильма с ID {}.", id);
        return filmService.findById(id);
    }

    @PostMapping
    public Film create(@Valid @RequestBody Film film) {
        log.info("Получен запрос на создание нового фильма: {}", film);
        return filmService.create(film);
    }

    @PutMapping
    public Film update(@Valid @RequestBody Film newFilm) {
        log.info("Получен запрос на обновление фильма с ID {}: {}", newFilm.getId(), newFilm);
        return filmService.update(newFilm);
    }

    @DeleteMapping("/{id}")
    public Film delete(@PathVariable Long id) {
        log.info("Получен запрос на удаление фильма с ID {}.", id);
        return filmService.delete(id);
    }

    @GetMapping("/popular")
    public Collection<Film> getTopFilms(@RequestParam(defaultValue = "10", required = false) int count) {
        log.info("Получен запрос на получение {} наиболее популярных фильмов.", count);
        return filmService.getTopFilms(count);
    }
}
