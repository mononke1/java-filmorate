package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.interfaces.FilmStorage;

import java.util.Collection;

@Service
public class FilmService {
    @Autowired
    @Qualifier("filmDbStorage")
    private FilmStorage filmStorage;

    public Collection<Film> getTopFilms(int limit) {
        return filmStorage.getTopFilms(limit);
    }

    public Collection<Film> getAllFilms() {
        return filmStorage.findAll();
    }

    public Film createFilm(Film film) {
        return filmStorage.create(film);
    }

    public Film updateFilm(Film newFilm) {
        return filmStorage.update(newFilm);
    }

    public Film deleteFilm(Long id) {
        return filmStorage.delete(id);
    }

    public Film getFilmById(Long id) {
        return filmStorage.findById(id);
    }
}
