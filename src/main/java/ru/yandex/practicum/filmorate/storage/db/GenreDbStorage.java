package ru.yandex.practicum.filmorate.storage.db;

import org.springframework.stereotype.Component;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.GenreRowMapper;
import ru.yandex.practicum.filmorate.model.Genre;

import java.util.List;

@Component
@Slf4j
@RequiredArgsConstructor
public class GenreDbStorage {
    private final JdbcTemplate jdbcTemplate;
    private final GenreRowMapper genreRowMapper;

    public List<Genre> findAll() {
        log.info("Получение списка всех жанров.");
        String sql = "SELECT * FROM genres ORDER BY genre_id";
        List<Genre> genres = jdbcTemplate.query(sql, genreRowMapper);
        log.debug("Найдено жанров: {}", genres.size());
        return genres;
    }

    public Genre findById(int id) {
        log.info("Получение жанра с ID {}", id);
        String sql = "SELECT * FROM genres WHERE genre_id = ?";
        try {
            Genre genre = jdbcTemplate.queryForObject(sql, genreRowMapper, id);
            log.debug("Жанр найден: {}", genre);
            return genre;
        } catch (EmptyResultDataAccessException e) {
            log.warn("Жанр с ID {} не найден", id);
            throw new NotFoundException("Жанр с ID " + id + " не найден.");
        }
    }
}
