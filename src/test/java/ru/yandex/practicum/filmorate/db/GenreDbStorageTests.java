package ru.yandex.practicum.filmorate.db;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.GenreRowMapper;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.db.GenreDbStorage;

import static org.assertj.core.api.Assertions.*;
import java.util.List;

@JdbcTest
@AutoConfigureTestDatabase
@Import({GenreDbStorage.class, GenreRowMapper.class})
class GenreDbStorageTests {

    @Autowired
    private GenreDbStorage genreStorage;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void setUp() {
        jdbcTemplate.update("DELETE FROM genres");

        jdbcTemplate.update("INSERT INTO genres (genre_id, genre_name) VALUES (1, 'Комедия')");
        jdbcTemplate.update("INSERT INTO genres (genre_id, genre_name) VALUES (2, 'Драма')");
        jdbcTemplate.update("INSERT INTO genres (genre_id, genre_name) VALUES (3, 'Мультфильм')");
        jdbcTemplate.update("INSERT INTO genres (genre_id, genre_name) VALUES (4, 'Триллер')");
        jdbcTemplate.update("INSERT INTO genres (genre_id, genre_name) VALUES (5, 'Документальный')");
        jdbcTemplate.update("INSERT INTO genres (genre_id, genre_name) VALUES (6, 'Боевик')");
    }

    @Test
    public void testFindAllGenres() {
        List<Genre> genres = genreStorage.findAll();

        assertThat(genres).isNotNull();
        assertThat(genres.size()).isEqualTo(6);
        assertThat(genres).extracting("name").containsExactly(
                "Комедия",
                "Драма",
                "Мультфильм",
                "Триллер",
                "Документальный",
                "Боевик"
        );
    }

    @Test
    public void testFindGenreById() {
        Genre genre = genreStorage.findById(1);

        assertThat(genre).isNotNull();
        assertThat(genre.getId()).isEqualTo(1);
        assertThat(genre.getName()).isEqualTo("Комедия");
    }

    @Test
    public void testFindGenreByIdNotFound() {
        int nonExistentGenreId = 999;

        assertThatThrownBy(() -> genreStorage.findById(nonExistentGenreId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Жанр с ID " + nonExistentGenreId + " не найден.");
    }
}
