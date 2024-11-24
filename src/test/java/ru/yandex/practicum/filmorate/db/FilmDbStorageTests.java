package ru.yandex.practicum.filmorate.db;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.mapper.GenreRowMapper;
import ru.yandex.practicum.filmorate.mapper.MpaRatingRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.RatingMpa;
import ru.yandex.practicum.filmorate.storage.db.FilmDbStorage;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDate;
import java.util.*;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmDbStorage.class, FilmRowMapper.class, GenreRowMapper.class, MpaRatingRowMapper.class})
class FilmDbStorageTests {

    @Autowired
    private FilmDbStorage filmStorage;

    @BeforeEach
    @Sql({"/schema.sql", "/data.sql"})
    void setUp() {
    }

    @Test
    public void testCreateFilm() {
        Film film = new Film();
        film.setName("Inception");
        film.setDescription("A mind-bending thriller");
        film.setReleaseDate(LocalDate.of(2010, 7, 16));
        film.setDuration(148L);
        film.setMpa(new RatingMpa(1, "G"));
        film.setGenres(new HashSet<>(Arrays.asList(new Genre(1, "Комедия"), new Genre(2, "Драма"))));

        Film createdFilm = filmStorage.create(film);

        assertThat(createdFilm.getId()).isNotNull();
        assertThat(createdFilm.getName()).isEqualTo("Inception");
        assertThat(createdFilm.getGenres()).hasSize(2);
        assertThat(createdFilm.getGenres()).containsExactlyInAnyOrder(
                new Genre(1, "Комедия"),
                new Genre(2, "Драма")
        );
    }

    @Test
    public void testFindFilmById() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Description");
        film.setReleaseDate(LocalDate.of(2021, 1, 1));
        film.setDuration(100L);
        film.setMpa(new RatingMpa(1, "G"));
        film.setGenres(new HashSet<>(Arrays.asList(
                new Genre(1, "Комедия"),
                new Genre(2, "Драма")
        )));

        // Сохраняем фильм и получаем сгенерированный ID
        Film createdFilm = filmStorage.create(film);
        Long filmId = createdFilm.getId();

        // Ищем фильм по ID
        Film foundFilm = filmStorage.findById(filmId);

        // Проверяем, что найденный фильм соответствует созданному
        assertThat(foundFilm).isNotNull();
        assertThat(foundFilm.getId()).isEqualTo(filmId);
        assertThat(foundFilm.getName()).isEqualTo("Test Film");
        assertThat(foundFilm.getGenres()).hasSize(2);
        assertThat(foundFilm.getGenres()).containsExactlyInAnyOrder(
                new Genre(1, "Комедия"),
                new Genre(2, "Драма")
        );
    }

    @Test
    public void testFindAllFilms() {
        Film film1 = new Film();
        film1.setName("Film One");
        film1.setDescription("First test film");
        film1.setReleaseDate(LocalDate.of(2020, 1, 1));
        film1.setDuration(100L);
        film1.setMpa(new RatingMpa(1, "G"));
        film1.setGenres(new HashSet<>(Arrays.asList(
                new Genre(1, "Комедия"),
                new Genre(2, "Драма")
        )));

        filmStorage.create(film1);

        Film film2 = new Film();
        film2.setName("Film Two");
        film2.setDescription("Second test film");
        film2.setReleaseDate(LocalDate.of(2021, 2, 2));
        film2.setDuration(110L);
        film2.setMpa(new RatingMpa(2, "PG"));
        film2.setGenres(new HashSet<>(Collections.singletonList(
                new Genre(3, "Мультфильм")
        )));

        filmStorage.create(film2);

        List<Film> films = filmStorage.findAll();

        assertThat(films).isNotEmpty();
        assertThat(films.size()).isEqualTo(2);

        assertThat(films).extracting("name").containsExactlyInAnyOrder("Film One", "Film Two");
    }

    @Test
    public void testUpdateFilm() {
        Film film = new Film();
        film.setName("Original Name");
        film.setDescription("Original Description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120L);
        film.setMpa(new RatingMpa(1, "G"));
        film.setGenres(new HashSet<>(Arrays.asList(new Genre(1, "Комедия"))));

        Film createdFilm = filmStorage.create(film);

        createdFilm.setName("Updated Name");
        createdFilm.setDescription("Updated Description");
        createdFilm.setGenres(new HashSet<>(Arrays.asList(
                new Genre(2, "Драма"),
                new Genre(3, "Мультфильм")
        )));

        Film updatedFilm = filmStorage.update(createdFilm);

        assertThat(updatedFilm.getName()).isEqualTo("Updated Name");
        assertThat(updatedFilm.getDescription()).isEqualTo("Updated Description");
        assertThat(updatedFilm.getGenres()).hasSize(2);
        assertThat(updatedFilm.getGenres()).containsExactlyInAnyOrder(
                new Genre(2, "Драма"),
                new Genre(3, "Мультфильм")
        );
    }

    @Test
    public void testDeleteFilm() {
        Film film = new Film();
        film.setName("Film to Delete");
        film.setDescription("This film will be deleted");
        film.setReleaseDate(LocalDate.of(2005, 5, 5));
        film.setDuration(100L);
        film.setMpa(new RatingMpa(1, "G"));

        Film createdFilm = filmStorage.create(film);

        Film deletedFilm = filmStorage.delete(createdFilm.getId());

        assertThat(deletedFilm).isNotNull();
        assertThat(deletedFilm.getId()).isEqualTo(createdFilm.getId());

        assertThatThrownBy(() -> filmStorage.findById(createdFilm.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("не найден");
    }
}
