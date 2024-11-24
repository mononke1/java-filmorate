package ru.yandex.practicum.filmorate.db;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.MpaRatingRowMapper;
import ru.yandex.practicum.filmorate.model.RatingMpa;
import ru.yandex.practicum.filmorate.storage.db.MpaDbStorage;

import static org.assertj.core.api.Assertions.*;
import java.util.List;

@JdbcTest
@AutoConfigureTestDatabase
@Import({MpaDbStorage.class, MpaRatingRowMapper.class})
class MpaDbStorageTests {

    @Autowired
    private MpaDbStorage mpaStorage;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void setUp() {
        jdbcTemplate.update("DELETE FROM rating_mpa");

        jdbcTemplate.update("INSERT INTO rating_mpa (rating_id, rating_name) VALUES (1, 'G')");
        jdbcTemplate.update("INSERT INTO rating_mpa (rating_id, rating_name) VALUES (2, 'PG')");
        jdbcTemplate.update("INSERT INTO rating_mpa (rating_id, rating_name) VALUES (3, 'PG-13')");
        jdbcTemplate.update("INSERT INTO rating_mpa (rating_id, rating_name) VALUES (4, 'R')");
        jdbcTemplate.update("INSERT INTO rating_mpa (rating_id, rating_name) VALUES (5, 'NC-17')");
    }

    @Test
    public void testFindAllRatings() {
        List<RatingMpa> ratings = mpaStorage.findAll();

        assertThat(ratings).isNotNull();
        assertThat(ratings.size()).isEqualTo(5);
        assertThat(ratings).extracting("name").containsExactly(
                "G",
                "PG",
                "PG-13",
                "R",
                "NC-17"
        );
    }

    @Test
    public void testFindRatingById() {
        RatingMpa rating = mpaStorage.findById(1);

        assertThat(rating).isNotNull();
        assertThat(rating.getId()).isEqualTo(1);
        assertThat(rating.getName()).isEqualTo("G");
    }

    @Test
    public void testFindRatingByIdNotFound() {
        int nonExistentRatingId = 999;

        assertThatThrownBy(() -> mpaStorage.findById(nonExistentRatingId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Рейтинг MPA с ID " + nonExistentRatingId + " не найден.");
    }
}
