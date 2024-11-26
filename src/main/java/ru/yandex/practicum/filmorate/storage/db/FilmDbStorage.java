package ru.yandex.practicum.filmorate.storage.db;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.mapper.GenreRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.interfaces.FilmStorage;
import ru.yandex.practicum.filmorate.util.DateUtil;
import org.springframework.transaction.annotation.Transactional;

import java.sql.PreparedStatement;
import java.sql.Statement;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Component("filmDbStorage")
@Slf4j
@RequiredArgsConstructor
public class FilmDbStorage implements FilmStorage {

    private final JdbcTemplate jdbc;
    private final FilmRowMapper mapper;
    private final GenreRowMapper genreRowMapper;
    private final NamedParameterJdbcTemplate namedParameterJdbcTemplate;

    @Override
    public List<Film> findAll() {
        log.info("Получен запрос на получение всех фильмов.");
        String query = "SELECT * FROM films AS fi JOIN rating_mpa AS ra ON fi.rating_id = ra.rating_id";
        List<Film> films = jdbc.query(query, mapper);
        log.debug("Найдено фильмов: {}", films.size());
        setGenreForFilm(films);
        log.debug("Фильмы после установки жанров и лайков: {}", films);
        return films;
    }

    @Override
    public Film findById(Long id) {
        log.info("Получен запрос на получение фильма с ID {}", id);
        String query = "SELECT fi.*, ra.rating_name FROM films AS fi " +
                "JOIN rating_mpa AS ra ON fi.rating_id = ra.rating_id " +
                "WHERE fi.film_id = ?";
        Film film;
        try {
            film = jdbc.queryForObject(query, mapper, id);
            log.debug("Фильм найден: {}", film);
        } catch (EmptyResultDataAccessException e) {
            log.warn("Фильм с ID {} не найден", id);
            throw new NotFoundException("Фильм с id=" + id + " не найден.");
        }

        String queryForGenre = "SELECT ge.genre_id, ge.genre_name FROM film_genres AS fg " +
                "JOIN genres AS ge ON fg.genre_id = ge.genre_id " +
                "WHERE fg.film_id = ?";
        List<Genre> genres = jdbc.query(queryForGenre, genreRowMapper, id);
        film.setGenres(new HashSet<>(genres));
        log.debug("Жанры фильма с ID {}: {}", id, genres);

        String queryForLike = "SELECT user_id FROM likes WHERE film_id = ?";
        List<Long> likes = jdbc.queryForList(queryForLike, Long.class, id);
        log.debug("Лайки фильма с ID {}: {}", id, likes);

        return film;
    }

    @Override
    public Collection<Film> getTopFilms(int limit) {
        log.info("Получен запрос на получение топ {} фильмов", limit);
        String sql = "SELECT fi.*, ra.rating_name " +
                "FROM films fi " +
                "JOIN rating_mpa ra ON fi.rating_id = ra.rating_id " +
                "LEFT JOIN likes li ON fi.film_id = li.film_id " +
                "GROUP BY fi.film_id " +
                "ORDER BY COUNT(li.user_id) DESC " +
                "LIMIT ?";
        List<Film> films = jdbc.query(sql, mapper, limit);
        log.debug("Топ фильмов получен: {}", films);
        setGenreForFilm(films);
        log.debug("Топ фильмов после установки жанров и лайков: {}", films);
        return films;
    }

    @Override
    @Transactional
    public Film create(Film film) {
        log.info("Получен запрос на создание фильма: {}", film);
        if (film == null) {
            log.error("Попытка создать фильм с null значением");
            throw new ValidationException("Фильм не может быть null.");
        }
        validateDate(film);
        String insertQuery = "INSERT INTO films(film_name, description, duration, release_date, rating_id) " +
                "VALUES (?, ?, ?, ?, ?)";
        long id = insert(insertQuery,
                film.getName(),
                film.getDescription(),
                film.getDuration(),
                film.getReleaseDate(),
                film.getMpa().getId());
        film.setId(id);
        log.debug("Фильм создан с ID {}", id);

        insertGenres(film);

        log.info("Фильм успешно создан: {}", film);
        return film;
    }

    @Override
    @Transactional
    public Film update(Film film) {
        log.info("Получен запрос на обновление фильма с ID {}: {}", film.getId(), film);
        if (film.getId() == null) {
            log.error("ID фильма не указан при попытке обновления");
            throw new ValidationException("Id должен быть указан.");
        }

        String updateQuery = "UPDATE films SET film_name = ?, description = ?, duration = ?, release_date = ?, rating_id = ? WHERE film_id = ?";

        int rowsUpdated = jdbc.update(
                updateQuery,
                film.getName(),
                film.getDescription(),
                film.getDuration(),
                film.getReleaseDate(),
                film.getMpa().getId(),
                film.getId()
        );

        if (rowsUpdated == 0) {
            log.warn("Фильм с ID {} не найден для обновления", film.getId());
            throw new NotFoundException("Фильм с id=" + film.getId() + " не найден.");
        }
        log.debug("Фильм с ID {} обновлен", film.getId());

        jdbc.update("DELETE FROM film_genres WHERE film_id = ?", film.getId());
        insertGenres(film);
        log.debug("Жанры фильма с ID {} обновлены", film.getId());

        jdbc.update("DELETE FROM likes WHERE film_id = ?", film.getId());
        log.debug("Лайки фильма с ID {} обновлены", film.getId());

        log.info("Фильм с ID {} успешно обновлен", film.getId());
        return film;
    }

    @Override
    @Transactional
    public Film delete(Long filmId) {
        log.info("Получен запрос на удаление фильма с ID {}", filmId);
        if (filmId == null) {
            log.error("ID фильма не может быть null при удалении");
            throw new IllegalArgumentException("Идентификатор фильма не может быть null.");
        }

        Film film = findById(filmId);

        jdbc.update("DELETE FROM likes WHERE film_id = ?", filmId);
        log.debug("Лайки фильма с ID {} удалены", filmId);
        jdbc.update("DELETE FROM film_genres WHERE film_id = ?", filmId);
        log.debug("Жанры фильма с ID {} удалены", filmId);

        int rowsAffected = jdbc.update("DELETE FROM films WHERE film_id = ?", filmId);

        if (rowsAffected == 0) {
            log.warn("Фильм с ID {} не найден для удаления", filmId);
            throw new NotFoundException("Фильм с id=" + filmId + " не найден.");
        }

        log.info("Фильм с ID {} успешно удален", filmId);
        return film;
    }

    private void update(String query, Object... params) {
        int rowsUpdated = jdbc.update(query, params);
        if (rowsUpdated == 0) {
            log.warn("Не удалось обновить данные по запросу: {}", query);
            throw new NotFoundException("Не удалось обновить данные. Объект не найден.");
        }
        log.debug("Данные успешно обновлены по запросу: {}", query);
    }

    private void setGenreForFilm(List<Film> films) {
        if (films.isEmpty()) {
            log.debug("Список фильмов пуст, установка жанров не требуется");
            return;
        }

        List<Long> filmIds = films.stream()
                .map(Film::getId)
                .collect(Collectors.toList());

        String sql = "SELECT fg.film_id, ge.genre_id, ge.genre_name FROM film_genres AS fg " +
                "JOIN genres AS ge ON fg.genre_id = ge.genre_id " +
                "WHERE fg.film_id IN (:filmIds)";

        Map<Long, Film> filmMap = films.stream()
                .collect(Collectors.toMap(Film::getId, Function.identity()));

        MapSqlParameterSource parameters = new MapSqlParameterSource();
        parameters.addValue("filmIds", filmIds);

        namedParameterJdbcTemplate.query(sql, parameters, rs -> {
            Long filmId = rs.getLong("film_id");
            int genreId = rs.getInt("genre_id");
            String genreName = rs.getString("genre_name");

            Genre genre = new Genre(genreId, genreName);

            Film film = filmMap.get(filmId);
            if (film != null) {
                if (film.getGenres() == null) {
                    film.setGenres(new HashSet<>());
                }
                film.getGenres().add(genre);
            }
        });
        log.debug("Жанры установлены для фильмов: {}", filmIds);
    }

    private void validateDate(Film film) {
        if (film.getReleaseDate() != null && film.getReleaseDate().isBefore(DateUtil.MIN_DATE)) {
            log.error("Некорректная дата релиза фильма с ID {}: {}", film.getId(), film.getReleaseDate());
            throw new ValidationException("Дата релиза должна быть не раньше 28 декабря 1895 года.");
        }
        log.debug("Дата релиза фильма с ID {} корректна: {}", film.getId(), film.getReleaseDate());
    }

    private void insertGenres(Film film) {
        if (film.getGenres() == null || film.getGenres().isEmpty()) {
            log.debug("Жанры не указаны для фильма с ID {}, пропуск вставки жанров", film.getId());
            return;
        }

        String query = "INSERT INTO film_genres(film_id, genre_id) VALUES (?, ?)";
        List<Object[]> batchArgs = film.getGenres().stream()
                .map(genre -> new Object[]{film.getId(), genre.getId()})
                .collect(Collectors.toList());
        jdbc.batchUpdate(query, batchArgs);
        log.debug("Жанры вставлены для фильма с ID {}: {}", film.getId(), film.getGenres());
    }

    private long insert(String query, Object... params) {
        GeneratedKeyHolder keyHolder = new GeneratedKeyHolder();
        jdbc.update(connection -> {
            PreparedStatement ps = connection
                    .prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            for (int idx = 0; idx < params.length; idx++) {
                ps.setObject(idx + 1, params[idx]);
            }
            return ps;
        }, keyHolder);

        Number key = keyHolder.getKey();
        if (key != null) {
            log.debug("Запрос на вставку выполнен, получен ID: {}", key.longValue());
            return key.longValue();
        } else {
            log.error("Не удалось получить сгенерированный ключ после вставки");
            throw new InternalServerException("Не удалось сохранить данные.");
        }
    }
}
