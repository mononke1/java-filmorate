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

    @Autowired
    private UserDbStorage userStorage;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    public void setUp() {
        jdbcTemplate.update("DELETE FROM friends");
        jdbcTemplate.update("DELETE FROM likes");
        jdbcTemplate.update("DELETE FROM users");
    }

    @Test
    public void testCreateUser() {
        User user = new User();
        user.setName("Test User");
        user.setLogin("testuser");
        user.setEmail("testuser@example.com");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User createdUser = userStorage.create(user);

        assertThat(createdUser.getId()).isNotNull();
        assertThat(createdUser.getName()).isEqualTo("Test User");
        assertThat(createdUser.getLogin()).isEqualTo("testuser");
        assertThat(createdUser.getEmail()).isEqualTo("testuser@example.com");
        assertThat(createdUser.getBirthday()).isEqualTo(LocalDate.of(1990, 1, 1));
    }

    @Test
    public void testFindUserById() {
        User user = new User();
        user.setName("Test User");
        user.setLogin("testuser");
        user.setEmail("testuser@example.com");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User createdUser = userStorage.create(user);

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
        User user1 = new User();
        user1.setName("User One");
        user1.setLogin("userone");
        user1.setEmail("userone@example.com");
        user1.setBirthday(LocalDate.of(1990, 1, 1));

        User user2 = new User();
        user2.setName("User Two");
        user2.setLogin("usertwo");
        user2.setEmail("usertwo@example.com");
        user2.setBirthday(LocalDate.of(1991, 2, 2));

        userStorage.create(user1);
        userStorage.create(user2);

        Collection<User> users = userStorage.findAll();

        assertThat(users).isNotEmpty();
        assertThat(users.size()).isEqualTo(2);

        assertThat(users).extracting("login").containsExactlyInAnyOrder("userone", "usertwo");
    }

    @Test
    public void testUpdateUser() {
        User user = new User();
        user.setName("Original Name");
        user.setLogin("originallogin");
        user.setEmail("original@example.com");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User createdUser = userStorage.create(user);

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
        User user = new User();
        user.setName("User to Delete");
        user.setLogin("todelete");
        user.setEmail("todelete@example.com");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User createdUser = userStorage.create(user);

        User deletedUser = userStorage.delete(createdUser.getId());

        assertThat(deletedUser).isNotNull();
        assertThat(deletedUser.getId()).isEqualTo(createdUser.getId());

        assertThatThrownBy(() -> userStorage.findById(createdUser.getId()))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("не найден");
    }

    @Test
    public void testAddAndRetrieveFriends() {
        User user1 = new User();
        user1.setName("User One");
        user1.setLogin("userone");
        user1.setEmail("userone@example.com");
        user1.setBirthday(LocalDate.of(1990, 1, 1));

        User user2 = new User();
        user2.setName("User Two");
        user2.setLogin("usertwo");
        user2.setEmail("usertwo@example.com");
        user2.setBirthday(LocalDate.of(1991, 2, 2));

        User createdUser1 = userStorage.create(user1);
        User createdUser2 = userStorage.create(user2);

        createdUser1.setFriends(new HashSet<>(Collections.singletonList(createdUser2.getId())));
        userStorage.update(createdUser1);

        User updatedUser1 = userStorage.findById(createdUser1.getId());

        assertThat(updatedUser1.getFriends()).isNotNull();
        assertThat(updatedUser1.getFriends()).containsExactly(createdUser2.getId());
    }
}
