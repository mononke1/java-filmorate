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
import ru.yandex.practicum.filmorate.mapper.UserRowMapper;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.db.FriendDbStorage;
import ru.yandex.practicum.filmorate.storage.db.UserDbStorage;

import static org.assertj.core.api.Assertions.*;
import java.time.LocalDate;
import java.util.*;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FriendDbStorage.class, UserDbStorage.class, UserRowMapper.class})
class FriendDbStorageTests {

    private final FriendDbStorage friendStorage;
    private final UserDbStorage userStorage;
    private final JdbcTemplate jdbcTemplate;

    private User user1;
    private User user2;
    private User user3;
    private User user4;

    @Autowired
    public FriendDbStorageTests(FriendDbStorage friendStorage, UserDbStorage userStorage, JdbcTemplate jdbcTemplate) {
        this.friendStorage = friendStorage;
        this.userStorage = userStorage;
        this.jdbcTemplate = jdbcTemplate;
    }

    @BeforeEach
    public void setUp() {
        jdbcTemplate.update("DELETE FROM friends");
        jdbcTemplate.update("DELETE FROM likes");
        jdbcTemplate.update("DELETE FROM users");

        user1 = createUser("User One", "userone", "userone@example.com", LocalDate.of(1990, 1, 1));
        user2 = createUser("User Two", "usertwo", "usertwo@example.com", LocalDate.of(1991, 2, 2));
        user3 = createUser("User Three", "userthree", "userthree@example.com", LocalDate.of(1992, 3, 3));
        user4 = createUser("User Four", "userfour", "userfour@example.com", LocalDate.of(1993, 4, 4));
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
    public void testAddFriend() {
        friendStorage.addFriend(user1.getId(), user2.getId());

        User updatedUser1 = userStorage.findById(user1.getId());
        assertThat(updatedUser1.getFriends()).isNotNull();
        assertThat(updatedUser1.getFriends()).containsExactly(user2.getId());

        User updatedUser2 = userStorage.findById(user2.getId());
        assertThat(updatedUser2.getFriends()).isNullOrEmpty();
    }

    @Test
    public void testRemoveFriend() {
        friendStorage.addFriend(user1.getId(), user2.getId());
        friendStorage.removeFriend(user1.getId(), user2.getId());

        User updatedUser1 = userStorage.findById(user1.getId());
        assertThat(updatedUser1.getFriends()).isNullOrEmpty();
    }

    @Test
    public void testGetFriends() {
        friendStorage.addFriend(user1.getId(), user2.getId());
        friendStorage.addFriend(user1.getId(), user3.getId());

        Collection<User> friends = friendStorage.getFriends(user1.getId());

        assertThat(friends).isNotNull();
        assertThat(friends.size()).isEqualTo(2);
        assertThat(friends).extracting("id").containsExactlyInAnyOrder(user2.getId(), user3.getId());
    }

    @Test
    public void testGetCommonFriends() {
        friendStorage.addFriend(user1.getId(), user3.getId());
        friendStorage.addFriend(user1.getId(), user4.getId());
        friendStorage.addFriend(user2.getId(), user3.getId());

        Collection<User> commonFriends = friendStorage.getCommonFriends(user1.getId(), user2.getId());

        assertThat(commonFriends).isNotNull();
        assertThat(commonFriends.size()).isEqualTo(1);
        assertThat(commonFriends).extracting("id").containsExactly(user3.getId());
    }

    @Test
    public void testAddSelfAsFriend() {
        assertThatThrownBy(() -> friendStorage.addFriend(user1.getId(), user1.getId()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Нельзя добавить самого себя в друзья");
    }

    @Test
    public void testAddNonExistentFriend() {
        Long nonExistentUserId = 999L;

        assertThatThrownBy(() -> friendStorage.addFriend(user1.getId(), nonExistentUserId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь с ID " + nonExistentUserId + " не найден");
    }

    @Test
    public void testRemoveNonExistentFriend() {
        Long nonExistentUserId = 999L;

        assertThatThrownBy(() -> friendStorage.removeFriend(user1.getId(), nonExistentUserId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь с ID " + nonExistentUserId + " не найден");
    }
}
