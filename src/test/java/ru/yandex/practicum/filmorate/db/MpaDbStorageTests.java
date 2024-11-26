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

    private final MpaDbStorage mpaStorage;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public MpaDbStorageTests(MpaDbStorage mpaStorage, JdbcTemplate jdbcTemplate) {
        this.mpaStorage = mpaStorage;
        this.jdbcTemplate = jdbcTemplate;
    }

    @BeforeEach
    public void setUp() {
        jdbcTemplate.update("DELETE FROM rating_mpa");

        insertRatingMpa(1, "G");
        insertRatingMpa(2, "PG");
        insertRatingMpa(3, "PG-13");
        insertRatingMpa(4, "R");
        insertRatingMpa(5, "NC-17");
    }

    private void insertRatingMpa(int id, String name) {
        jdbcTemplate.update("INSERT INTO rating_mpa (rating_id, rating_name) VALUES (?, ?)", id, name);
    }

    @Test
    public void testFindAllRatings() {
        List<RatingMpa> ratings = mpaStorage.findAll();

        assertThat(ratings).isNotNull();
        assertThat(ratings).hasSize(5);
        assertThat(ratings).extracting(RatingMpa::getName).containsExactly(
                "G", "PG", "PG-13", "R", "NC-17"
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
