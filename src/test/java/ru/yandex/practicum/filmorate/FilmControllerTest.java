package ru.yandex.practicum.filmorate;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class FilmControllerTest {

    @LocalServerPort
    private int port;

    @Autowired
    private TestRestTemplate restTemplate;

    private Film film;
    private Film newFilm;
    private String url;

    @BeforeEach
    public void setUp() {
        // Инициализация URL для запросов
        url = "http://localhost:" + port + "/films";

        // Инициализация объектов Film для каждого теста
        film = new Film();
        film.setName("Test Film");
        film.setDescription("Test Film");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120L);

        newFilm = new Film();
        newFilm.setName("Test Film - new");
        newFilm.setDescription("Test Film - new");
        newFilm.setReleaseDate(LocalDate.of(2010, 1, 1));
        newFilm.setDuration(200L);
    }

    @Test
    public void testCreateAndUpdateFilm() {
        // Создание нового фильма
        ResponseEntity<Film> response = restTemplate.postForEntity(url, film, Film.class);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Test Film", response.getBody().getName());
        assertEquals(LocalDate.of(2020, 1, 1), response.getBody().getReleaseDate());
        assertEquals(120L, response.getBody().getDuration());
        assertEquals("Test Film", response.getBody().getDescription());

        Long createdFilmId = response.getBody().getId();
        assertNotNull(createdFilmId);

        // Обновление фильма
        newFilm.setId(createdFilmId);
        restTemplate.put(url, newFilm);

        // Проверка обновленного фильма
        ResponseEntity<Film[]> updatedResponse = restTemplate.getForEntity(url, Film[].class);

        assertEquals(HttpStatus.OK, updatedResponse.getStatusCode());
        assertNotNull(updatedResponse.getBody());

        Film[] films = updatedResponse.getBody();
        assertTrue(films.length > 0);

        Film updatedFilm = films[0];

        assertEquals("Test Film - new", updatedFilm.getName());
        assertEquals("Test Film - new", updatedFilm.getDescription());
        assertEquals(LocalDate.of(2010, 1, 1), updatedFilm.getReleaseDate());
        assertEquals(200L, updatedFilm.getDuration());
    }
}
