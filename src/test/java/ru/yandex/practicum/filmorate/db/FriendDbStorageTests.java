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

    @Autowired
    private FriendDbStorage friendStorage;

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
    public void testAddFriend() {
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

        friendStorage.addFriend(createdUser1.getId(), createdUser2.getId());

        User updatedUser1 = userStorage.findById(createdUser1.getId());

        assertThat(updatedUser1.getFriends()).isNotNull();
        assertThat(updatedUser1.getFriends()).containsExactly(createdUser2.getId());

        User updatedUser2 = userStorage.findById(createdUser2.getId());

        assertThat(updatedUser2.getFriends()).isNullOrEmpty();
    }

    @Test
    public void testRemoveFriend() {
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

        friendStorage.addFriend(createdUser1.getId(), createdUser2.getId());

        friendStorage.removeFriend(createdUser1.getId(), createdUser2.getId());

        User updatedUser1 = userStorage.findById(createdUser1.getId());

        assertThat(updatedUser1.getFriends()).isNullOrEmpty();
    }

    @Test
    public void testGetFriends() {
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

        User user3 = new User();
        user3.setName("User Three");
        user3.setLogin("userthree");
        user3.setEmail("userthree@example.com");
        user3.setBirthday(LocalDate.of(1992, 3, 3));

        User createdUser1 = userStorage.create(user1);
        User createdUser2 = userStorage.create(user2);
        User createdUser3 = userStorage.create(user3);

        friendStorage.addFriend(createdUser1.getId(), createdUser2.getId());
        friendStorage.addFriend(createdUser1.getId(), createdUser3.getId());

        Collection<User> friends = friendStorage.getFriends(createdUser1.getId());

        assertThat(friends).isNotNull();
        assertThat(friends.size()).isEqualTo(2);
        assertThat(friends).extracting("id").containsExactlyInAnyOrder(createdUser2.getId(), createdUser3.getId());
    }

    @Test
    public void testGetCommonFriends() {
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

        User user3 = new User();
        user3.setName("User Three");
        user3.setLogin("userthree");
        user3.setEmail("userthree@example.com");
        user3.setBirthday(LocalDate.of(1992, 3, 3));

        User user4 = new User();
        user4.setName("User Four");
        user4.setLogin("userfour");
        user4.setEmail("userfour@example.com");
        user4.setBirthday(LocalDate.of(1993, 4, 4));

        User createdUser1 = userStorage.create(user1);
        User createdUser2 = userStorage.create(user2);
        User createdUser3 = userStorage.create(user3);
        User createdUser4 = userStorage.create(user4);

        friendStorage.addFriend(createdUser1.getId(), createdUser3.getId());
        friendStorage.addFriend(createdUser1.getId(), createdUser4.getId());
        friendStorage.addFriend(createdUser2.getId(), createdUser3.getId());

        Collection<User> commonFriends = friendStorage.getCommonFriends(createdUser1.getId(), createdUser2.getId());

        assertThat(commonFriends).isNotNull();
        assertThat(commonFriends.size()).isEqualTo(1);
        assertThat(commonFriends).extracting("id").containsExactly(createdUser3.getId());
    }

    @Test
    public void testAddSelfAsFriend() {
        User user = new User();
        user.setName("User");
        user.setLogin("user");
        user.setEmail("user@example.com");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User createdUser = userStorage.create(user);

        assertThatThrownBy(() -> friendStorage.addFriend(createdUser.getId(), createdUser.getId()))
                .isInstanceOf(ValidationException.class)
                .hasMessageContaining("Нельзя добавить самого себя в друзья");
    }

    @Test
    public void testAddNonExistentFriend() {
        User user = new User();
        user.setName("User");
        user.setLogin("user");
        user.setEmail("user@example.com");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User createdUser = userStorage.create(user);

        Long nonExistentUserId = 999L;

        assertThatThrownBy(() -> friendStorage.addFriend(createdUser.getId(), nonExistentUserId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь с ID " + nonExistentUserId + " не найден");
    }

    @Test
    public void testRemoveNonExistentFriend() {
        User user = new User();
        user.setName("User");
        user.setLogin("user");
        user.setEmail("user@example.com");
        user.setBirthday(LocalDate.of(1990, 1, 1));

        User createdUser = userStorage.create(user);

        Long nonExistentUserId = 999L;

        assertThatThrownBy(() -> friendStorage.removeFriend(createdUser.getId(), nonExistentUserId))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Пользователь с ID " + nonExistentUserId + " не найден");
    }
}
