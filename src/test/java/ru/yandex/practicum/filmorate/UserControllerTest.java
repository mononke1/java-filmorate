package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class UserControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private User user;
    private User newUser;
    private String url;

    @BeforeEach
    public void setUp() {
        // Инициализация URL для запросов
        url = "http://localhost:" + port + "/users";

        // Инициализация объектов User для каждого теста
        user = new User();
        user.setName("Test Film");
        user.setLogin("TestFilm");
        user.setBirthday(LocalDate.of(2020, 1, 1));
        user.setEmail("test@test.ru");

        newUser = new User();
        newUser.setName("Test Film - new");
        newUser.setLogin("Test");
        newUser.setBirthday(LocalDate.of(2019, 1, 1));
        newUser.setEmail("NewTest@test.ru");
    }

    @Test
    public void testCreateAndUpdateUser() {
        // Создание нового пользователя
        ResponseEntity<User> response = restTemplate.postForEntity(url, user, User.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Test Film", response.getBody().getName());
        assertEquals(LocalDate.of(2020, 1, 1), response.getBody().getBirthday());
        assertEquals("TestFilm", response.getBody().getLogin());
        assertEquals("test@test.ru", response.getBody().getEmail());

        Long createdUserId = response.getBody().getId();
        assertNotNull(createdUserId);

        // Обновление пользователя
        newUser.setId(createdUserId);
        restTemplate.put(url, newUser);

        // Проверка обновленного пользователя
        ResponseEntity<User[]> updatedResponse = restTemplate.getForEntity(url, User[].class);

        assertEquals(HttpStatus.OK, updatedResponse.getStatusCode());
        assertNotNull(updatedResponse.getBody());

        User[] users = updatedResponse.getBody();
        assertTrue(users.length > 0);

        User updatedUser = users[0];

        assertEquals("Test Film - new", updatedUser.getName());
        assertEquals(LocalDate.of(2019, 1, 1), updatedUser.getBirthday());
        assertEquals("Test", updatedUser.getLogin());
        assertEquals("NewTest@test.ru", updatedUser.getEmail());
    }
}
