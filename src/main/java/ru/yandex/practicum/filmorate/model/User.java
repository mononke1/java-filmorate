package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class User {
    private static final String FORMAT = "yyyy-MM-dd";
    private String name;
    private Long id;

    @NotEmpty(message = "Адрес электронной почты не может быть пустым")
    @Email(message = "Некорректный адрес электронной почты")
    private String email;

    @Pattern(regexp = "^[^\\s]+$", message = "Логин не должн содержать пробелы")
    private String login;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = FORMAT)
    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    @NotNull(message = "Дата рождения не может быть null")
    private LocalDate birthday;
}
