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

    private FilmDbStorage filmStorage;

    @Autowired
    public FilmDbStorageTests(FilmDbStorage filmStorage) {
        this.filmStorage = filmStorage;
    }

    private Film film1;
    private Film film2;

    @BeforeEach
    @Sql({"/schema.sql", "/data.sql"})
    void setUp() {
        film1 = new Film();
        film1.setName("Film One");
        film1.setDescription("First test film");
        film1.setReleaseDate(LocalDate.of(2020, 1, 1));
        film1.setDuration(100L);
        film1.setMpa(new RatingMpa(1, "G"));
        film1.setGenres(new HashSet<>(Arrays.asList(
                new Genre(1, "Комедия"),
                new Genre(2, "Драма")
        )));

        film2 = new Film();
        film2.setName("Film Two");
        film2.setDescription("Second test film");
        film2.setReleaseDate(LocalDate.of(2021, 2, 2));
        film2.setDuration(110L);
        film2.setMpa(new RatingMpa(2, "PG"));
        film2.setGenres(new HashSet<>(Collections.singletonList(
                new Genre(3, "Мультфильм")
        )));
    }

    @Test
    public void testCreateFilm() {
        Film createdFilm = filmStorage.create(film1);

        assertThat(createdFilm.getId()).isNotNull();
        assertThat(createdFilm.getName()).isEqualTo(film1.getName());
        assertThat(createdFilm.getGenres()).hasSize(2);
        assertThat(createdFilm.getGenres()).containsExactlyInAnyOrder(
                new Genre(1, "Комедия"),
                new Genre(2, "Драма")
        );
    }

    @Test
    public void testFindFilmById() {
        Film createdFilm = filmStorage.create(film1);
        Long filmId = createdFilm.getId();

        Film foundFilm = filmStorage.findById(filmId);

        assertThat(foundFilm).isNotNull();
        assertThat(foundFilm.getId()).isEqualTo(filmId);
        assertThat(foundFilm.getName()).isEqualTo(film1.getName());

        assertThat(foundFilm.getGenres()).isNotNull();
        assertThat(foundFilm.getGenres()).hasSize(2);
        assertThat(foundFilm.getGenres()).containsExactlyInAnyOrder(
                new Genre(1, "Комедия"),
                new Genre(2, "Драма")
        );
    }

    @Test
    public void testFindAllFilms() {
        filmStorage.create(film1);
        filmStorage.create(film2);

        List<Film> films = filmStorage.findAll();

        assertThat(films).isNotEmpty();
        assertThat(films.size()).isEqualTo(2);

        assertThat(films).extracting("name").containsExactlyInAnyOrder(film1.getName(), film2.getName());
    }

    @Test
    public void testUpdateFilm() {
        Film createdFilm = filmStorage.create(film1);

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
        Film createdFilm = filmStorage.create(film1);

        Film deletedFilm = filmStorage.delete(createdFilm.getId());

        assertThat(deletedFilm).isNotNull();
        assertThat(deletedFilm.getId()).isEqualTo(createdFilm.getId());

        assertThatThrownBy(() -> filmStorage.findById(createdFilm.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("не найден");
    }
}
