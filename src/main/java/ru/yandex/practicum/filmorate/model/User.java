package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;
import ru.yandex.practicum.filmorate.util.DateConstants;

import java.time.LocalDate;

@Data
public class User {
    private final String FORMAT = DateConstants.FORMAT;
    private Long id;
    private String name;

    @NotEmpty(message = "Адрес электронной почты не может быть пустым")
    @Email(message = "Некорректный адрес электронной почты")
    private String email;

    @Pattern(regexp = "^[^\\s]+$", message = "Логин не должн содержать пробелы")
    private String login;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = FORMAT)
    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;
}
