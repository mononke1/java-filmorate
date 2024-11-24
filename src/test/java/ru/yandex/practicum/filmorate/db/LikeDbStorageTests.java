package ru.yandex.practicum.filmorate.db;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
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

    @Autowired
    private LikeDbStorage likeStorage;

    @Autowired
    private FilmDbStorage filmStorage;

    @Autowired
    private UserDbStorage userStorage;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void setUp() {
        jdbcTemplate.update("DELETE FROM likes");
        jdbcTemplate.update("DELETE FROM film_genres");
        jdbcTemplate.update("DELETE FROM films");
        jdbcTemplate.update("DELETE FROM users");
        jdbcTemplate.update("DELETE FROM genres");
        jdbcTemplate.update("DELETE FROM rating_mpa");

        jdbcTemplate.update("INSERT INTO genres (genre_id, genre_name) VALUES (1, 'Комедия')");
        jdbcTemplate.update("INSERT INTO genres (genre_id, genre_name) VALUES (2, 'Драма')");
        jdbcTemplate.update("INSERT INTO rating_mpa (rating_id, rating_name) VALUES (1, 'G')");
        jdbcTemplate.update("INSERT INTO rating_mpa (rating_id, rating_name) VALUES (2, 'PG')");
    }

    @Test
    public void testAddLike() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("A test film");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120L);
        film.setMpa(new RatingMpa(1, "G"));
        film.setGenres(new HashSet<>(Arrays.asList(
                new Genre(1, "Комедия"),
                new Genre(2, "Драма")
        )));
        Film createdFilm = filmStorage.create(film);

        User user = new User();
        user.setName("Test User");
        user.setLogin("testuser");
        user.setEmail("testuser@example.com");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User createdUser = userStorage.create(user);

        Film updatedFilm = likeStorage.addLike(createdFilm.getId(), createdUser.getId());

        assertThat(updatedFilm.getLikes()).contains(createdUser.getId());

        Film filmFromDb = filmStorage.findById(createdFilm.getId());
        assertThat(filmFromDb.getLikes()).containsExactly(createdUser.getId());
    }

    @Test
    public void testRemoveLike() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("A test film");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120L);
        film.setMpa(new RatingMpa(1, "G"));
        film.setGenres(new HashSet<>(Collections.singletonList(
                new Genre(1, "Комедия")
        )));
        Film createdFilm = filmStorage.create(film);

        User user = new User();
        user.setName("Test User");
        user.setLogin("testuser");
        user.setEmail("testuser@example.com");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User createdUser = userStorage.create(user);

        likeStorage.addLike(createdFilm.getId(), createdUser.getId());

        Film updatedFilm = likeStorage.removeLike(createdFilm.getId(), createdUser.getId());

        assertThat(updatedFilm.getLikes()).doesNotContain(createdUser.getId());

        Film filmFromDb = filmStorage.findById(createdFilm.getId());
        assertThat(filmFromDb.getLikes()).isEmpty();
    }

    @Test
    public void testRemoveLikeNotExists() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("A test film");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120L);
        film.setMpa(new RatingMpa(1, "G"));
        film.setGenres(new HashSet<>(Collections.singletonList(
                new Genre(1, "Комедия")
        )));
        Film createdFilm = filmStorage.create(film);

        User user = new User();
        user.setName("Test User");
        user.setLogin("testuser");
        user.setEmail("testuser@example.com");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User createdUser = userStorage.create(user);

        assertThatThrownBy(() -> likeStorage.removeLike(createdFilm.getId(), createdUser.getId()))
                .isInstanceOf( NotFoundException.class)
                .hasMessageContaining("Лайк от пользователя с ID " + createdUser.getId() + " к фильму с ID " + createdFilm.getId() + " не найден.");
    }

    @Test
    public void testAddLikeFilmNotFound() {
        User user = new User();
        user.setName("Test User");
        user.setLogin("testuser");
        user.setEmail("testuser@example.com");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User createdUser = userStorage.create(user);

        Long nonExistentFilmId = 999L;

        assertThatThrownBy(() -> likeStorage.addLike(nonExistentFilmId, createdUser.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Фильм с ID " + nonExistentFilmId + " не найден.");
    }

    @Test
    public void testAddLikeUserNotFound() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("A test film");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120L);
        film.setMpa(new RatingMpa(1, "G"));
        film.setGenres(new HashSet<>(Collections.singletonList(
                new Genre(1, "Комедия")
        )));
        Film createdFilm = filmStorage.create(film);

        Long nonExistentUserId = 999L;

        assertThatThrownBy(() -> likeStorage.addLike(createdFilm.getId(), nonExistentUserId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь с ID " + nonExistentUserId + " не найден.");
    }

    @Test
    public void testRemoveLikeFilmNotFound() {
        User user = new User();
        user.setName("Test User");
        user.setLogin("testuser");
        user.setEmail("testuser@example.com");
        user.setBirthday(LocalDate.of(1990, 1, 1));
        User createdUser = userStorage.create(user);

        Long nonExistentFilmId = 999L;

        assertThatThrownBy(() -> likeStorage.removeLike(nonExistentFilmId, createdUser.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Фильм с ID " + nonExistentFilmId + " не найден.");
    }

    @Test
    public void testRemoveLikeUserNotFound() {
        Film film = new Film();
        film.setName("Test Film");
        film.setDescription("A test film");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120L);
        film.setMpa(new RatingMpa(1, "G"));
        film.setGenres(new HashSet<>(Collections.singletonList(
                new Genre(1, "Комедия")
        )));
        Film createdFilm = filmStorage.create(film);

        Long nonExistentUserId = 999L;

        assertThatThrownBy(() -> likeStorage.removeLike(createdFilm.getId(), nonExistentUserId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь с ID " + nonExistentUserId + " не найден.");
    }
}
