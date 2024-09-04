package ru.yandex.practicum.filmorate;

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

    @Test
    public void testCreateAndUpdateUser() {
        User user = new User();
        user.setName("Test Film");
        user.setLogin("TestFilm");
        user.setBirthday(LocalDate.of(2020, 1, 1));
        user.setEmail("test@test.ru");

        String url = "http://localhost:" + port + "/users";

        ResponseEntity<User> response = restTemplate.postForEntity(url, user, User.class);


        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Test Film", response.getBody().getName());
        assertEquals(LocalDate.of(2020, 1, 1), response.getBody().getBirthday());
        assertEquals("TestFilm", response.getBody().getLogin());
        assertEquals("test@test.ru", response.getBody().getEmail());

        Long createdFilmId = response.getBody().getId();
        assertNotNull(createdFilmId);

        User newUser = new User();
        newUser.setId(createdFilmId);
        newUser.setName("Test Film - new");
        newUser.setLogin("Test");
        newUser.setBirthday(LocalDate.of(2019, 1, 1));
        newUser.setEmail("NewTest@test.ru");

        restTemplate.put(url, newUser);

        ResponseEntity<User[]> updatedResponse = restTemplate.getForEntity(url, User[].class);

        assertEquals(HttpStatus.OK, updatedResponse.getStatusCode());
        assertNotNull(updatedResponse.getBody());

        User[] users = updatedResponse.getBody();
        assertTrue(users.length > 0);

        User updatedFilm = users[0];

        assertEquals("Test Film - new", updatedFilm.getName());
        assertEquals(LocalDate.of(2019, 1, 1), updatedFilm.getBirthday());
        assertEquals("Test", updatedFilm.getLogin());
        assertEquals("NewTest@test.ru", updatedFilm.getEmail());
    }
}
