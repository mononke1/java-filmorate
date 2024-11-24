package ru.yandex.practicum.filmorate.storage.db;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.MpaRatingRowMapper;
import ru.yandex.practicum.filmorate.model.RatingMpa;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class MpaDbStorage {
    private final JdbcTemplate jdbcTemplate;
    private final MpaRatingRowMapper mpaRatingRowMapper;

    public List<RatingMpa> findAll() {
        log.info("Получение списка всех рейтингов MPA.");
        String sql = "SELECT * FROM rating_mpa ORDER BY rating_id";
        List<RatingMpa> ratings = jdbcTemplate.query(sql, mpaRatingRowMapper);
        log.debug("Найдено рейтингов: {}", ratings.size());
        return ratings;
    }

    public RatingMpa findById(int id) {
        log.info("Получение рейтинга MPA с ID {}", id);
        String sql = "SELECT * FROM rating_mpa WHERE rating_id = ?";
        try {
            RatingMpa rating = jdbcTemplate.queryForObject(sql, mpaRatingRowMapper, id);
            log.debug("Рейтинг найден: {}", rating);
            return rating;
        } catch (EmptyResultDataAccessException e) {
            log.warn("Рейтинг MPA с ID {} не найден", id);
            throw new NotFoundException("Рейтинг MPA с ID " + id + " не найден.");
        }
    }
}
