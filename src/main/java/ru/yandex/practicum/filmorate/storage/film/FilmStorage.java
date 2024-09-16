package ru.yandex.practicum.filmorate.storage.film;

import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;

public interface FilmStorage {

    public Collection<Film> findAll();

    public Film create(Film film);

    public Film update(Film newFilm);

    public Film delete(Long id);

    public Film findById(Long id);
}
