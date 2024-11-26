package ru.yandex.practicum.filmorate.db;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.FilmRowMapper;
import ru.yandex.practicum.filmorate.mapper.GenreRowMapper;
import ru.yandex.practicum.filmorate.mapper.MpaRatingRowMapper;
import ru.yandex.practicum.filmorate.mapper.UserRowMapper;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.RatingMpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.db.FilmDbStorage;
import ru.yandex.practicum.filmorate.storage.db.LikeDbStorage;
import ru.yandex.practicum.filmorate.storage.db.UserDbStorage;

import static org.assertj.core.api.Assertions.*;
import java.time.LocalDate;
import java.util.*;

@JdbcTest
@AutoConfigureTestDatabase
@Import({
        LikeDbStorage.class,
        FilmDbStorage.class,
        UserDbStorage.class,
        FilmRowMapper.class,
        GenreRowMapper.class,
        MpaRatingRowMapper.class,
        UserRowMapper.class
})
class LikeDbStorageTests {

    private final LikeDbStorage likeStorage;
    private final FilmDbStorage filmStorage;
    private final UserDbStorage userStorage;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public LikeDbStorageTests(LikeDbStorage likeStorage,
                              FilmDbStorage filmStorage,
                              UserDbStorage userStorage,
                              JdbcTemplate jdbcTemplate) {
        this.likeStorage = likeStorage;
        this.filmStorage = filmStorage;
        this.userStorage = userStorage;
        this.jdbcTemplate = jdbcTemplate;
    }

    @BeforeEach
    public void setUp() {
        jdbcTemplate.update("DELETE FROM likes");
        jdbcTemplate.update("DELETE FROM film_genres");
        jdbcTemplate.update("DELETE FROM films");
        jdbcTemplate.update("DELETE FROM users");
        jdbcTemplate.update("DELETE FROM genres");
        jdbcTemplate.update("DELETE FROM rating_mpa");

        insertGenre(1, "Комедия");
        insertGenre(2, "Драма");
        insertRatingMpa(1, "G");
        insertRatingMpa(2, "PG");
    }

    private void insertGenre(int id, String name) {
        jdbcTemplate.update("INSERT INTO genres (genre_id, genre_name) VALUES (?, ?)", id, name);
    }

    private void insertRatingMpa(int id, String name) {
        jdbcTemplate.update("INSERT INTO rating_mpa (rating_id, rating_name) VALUES (?, ?)", id, name);
    }

    private User createUser(String name, String login, String email, LocalDate birthday) {
        User user = new User();
        user.setName(name);
        user.setLogin(login);
        user.setEmail(email);
        user.setBirthday(birthday);
        return userStorage.create(user);
    }

    private Film createFilm(String name, String description, LocalDate releaseDate, Long duration, RatingMpa mpa, Set<Genre> genres) {
        Film film = new Film();
        film.setName(name);
        film.setDescription(description);
        film.setReleaseDate(releaseDate);
        film.setDuration(duration);
        film.setMpa(mpa);
        film.setGenres(genres);
        return filmStorage.create(film);
    }

    @Test
    public void testRemoveLikeNotExists() {
        Film film = createFilm("Test Film", "A test film", LocalDate.of(2020, 1, 1), 120L, new RatingMpa(1, "G"),
                new HashSet<>(Collections.singletonList(new Genre(1, "Комедия"))));
        User user = createUser("Test User", "testuser", "testuser@example.com", LocalDate.of(1990, 1, 1));

        assertThatThrownBy(() -> likeStorage.removeLike(film.getId(), user.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Лайк от пользователя с ID " + user.getId() + " к фильму с ID " + film.getId() + " не найден.");
    }

    @Test
    public void testAddLikeFilmNotFound() {
        User user = createUser("Test User", "testuser", "testuser@example.com", LocalDate.of(1990, 1, 1));

        Long nonExistentFilmId = 999L;

        assertThatThrownBy(() -> likeStorage.addLike(nonExistentFilmId, user.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Фильм с ID " + nonExistentFilmId + " не найден.");
    }

    @Test
    public void testAddLikeUserNotFound() {
        Film film = createFilm("Test Film", "A test film", LocalDate.of(2020, 1, 1), 120L, new RatingMpa(1, "G"),
                new HashSet<>(Collections.singletonList(new Genre(1, "Комедия"))));

        Long nonExistentUserId = 999L;

        assertThatThrownBy(() -> likeStorage.addLike(film.getId(), nonExistentUserId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь с ID " + nonExistentUserId + " не найден.");
    }

    @Test
    public void testRemoveLikeFilmNotFound() {
        User user = createUser("Test User", "testuser", "testuser@example.com", LocalDate.of(1990, 1, 1));

        Long nonExistentFilmId = 999L;

        assertThatThrownBy(() -> likeStorage.removeLike(nonExistentFilmId, user.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Фильм с ID " + nonExistentFilmId + " не найден.");
    }

    @Test
    public void testRemoveLikeUserNotFound() {
        Film film = createFilm("Test Film", "A test film", LocalDate.of(2020, 1, 1), 120L, new RatingMpa(1, "G"),
                new HashSet<>(Collections.singletonList(new Genre(1, "Комедия"))));

        Long nonExistentUserId = 999L;

        assertThatThrownBy(() -> likeStorage.removeLike(film.getId(), nonExistentUserId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь с ID " + nonExistentUserId + " не найден.");
    }
}
