package ru.yandex.practicum.filmorate.db;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.mapper.UserRowMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.db.UserDbStorage;

import static org.assertj.core.api.Assertions.*;
import java.time.LocalDate;
import java.util.*;

@JdbcTest
@AutoConfigureTestDatabase
@Import({UserDbStorage.class, UserRowMapper.class})
class UserDbStorageTests {

    private final UserDbStorage userStorage;
    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UserDbStorageTests(UserDbStorage userStorage, JdbcTemplate jdbcTemplate) {
        this.userStorage = userStorage;
        this.jdbcTemplate = jdbcTemplate;
    }

    @BeforeEach
    public void setUp() {
        jdbcTemplate.update("DELETE FROM friends");
        jdbcTemplate.update("DELETE FROM likes");
        jdbcTemplate.update("DELETE FROM users");
    }

    private User createUser(String name, String login, String email, LocalDate birthday) {
        User user = new User();
        user.setName(name);
        user.setLogin(login);
        user.setEmail(email);
        user.setBirthday(birthday);
        return userStorage.create(user);
    }

    @Test
    public void testCreateUser() {
        User createdUser = createUser("Test User", "testuser", "testuser@example.com", LocalDate.of(1990, 1, 1));

        assertThat(createdUser.getId()).isNotNull();
        assertThat(createdUser.getName()).isEqualTo("Test User");
        assertThat(createdUser.getLogin()).isEqualTo("testuser");
        assertThat(createdUser.getEmail()).isEqualTo("testuser@example.com");
        assertThat(createdUser.getBirthday()).isEqualTo(LocalDate.of(1990, 1, 1));
    }

    @Test
    public void testFindUserById() {
        User createdUser = createUser("Test User", "testuser", "testuser@example.com", LocalDate.of(1990, 1, 1));

        User foundUser = userStorage.findById(createdUser.getId());

        assertThat(foundUser).isNotNull();
        assertThat(foundUser.getId()).isEqualTo(createdUser.getId());
        assertThat(foundUser.getName()).isEqualTo("Test User");
        assertThat(foundUser.getLogin()).isEqualTo("testuser");
        assertThat(foundUser.getEmail()).isEqualTo("testuser@example.com");
        assertThat(foundUser.getBirthday()).isEqualTo(LocalDate.of(1990, 1, 1));
    }

    @Test
    public void testFindAllUsers() {
        User user1 = createUser("User One", "userone", "userone@example.com", LocalDate.of(1990, 1, 1));
        User user2 = createUser("User Two", "usertwo", "usertwo@example.com", LocalDate.of(1991, 2, 2));

        Collection<User> users = userStorage.findAll();

        assertThat(users).isNotEmpty();
        assertThat(users).hasSize(2);
        assertThat(users).extracting(User::getLogin).containsExactlyInAnyOrder("userone", "usertwo");
    }

    @Test
    public void testUpdateUser() {
        User createdUser = createUser("Original Name", "originallogin", "original@example.com", LocalDate.of(1990, 1, 1));

        createdUser.setName("Updated Name");
        createdUser.setLogin("updatedlogin");
        createdUser.setEmail("updated@example.com");
        createdUser.setBirthday(LocalDate.of(1992, 3, 3));

        User updatedUser = userStorage.update(createdUser);

        assertThat(updatedUser.getName()).isEqualTo("Updated Name");
        assertThat(updatedUser.getLogin()).isEqualTo("updatedlogin");
        assertThat(updatedUser.getEmail()).isEqualTo("updated@example.com");
        assertThat(updatedUser.getBirthday()).isEqualTo(LocalDate.of(1992, 3, 3));
    }

    @Test
    public void testDeleteUser() {
        User createdUser = createUser("User to Delete", "todelete", "todelete@example.com", LocalDate.of(1990, 1, 1));

        User deletedUser = userStorage.delete(createdUser.getId());

        assertThat(deletedUser).isNotNull();
        assertThat(deletedUser.getId()).isEqualTo(createdUser.getId());

        assertThatThrownBy(() -> userStorage.findById(createdUser.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("не найден");
    }

    @Test
    public void testAddAndRetrieveFriends() {
        User user1 = createUser("User One", "userone", "userone@example.com", LocalDate.of(1990, 1, 1));
        User user2 = createUser("User Two", "usertwo", "usertwo@example.com", LocalDate.of(1991, 2, 2));

        user1.setFriends(new HashSet<>(Collections.singletonList(user2.getId())));
        userStorage.update(user1);

        User updatedUser1 = userStorage.findById(user1.getId());

        assertThat(updatedUser1.getFriends()).isNotNull();
        assertThat(updatedUser1.getFriends()).containsExactly(user2.getId());
    }
}
