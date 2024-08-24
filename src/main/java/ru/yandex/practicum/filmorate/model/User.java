package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class User {
    String name;
    Long id;

    @Email(message = "Некорректный адрес электронной почты")
    String email;

    @Pattern(regexp = "^[^\\s]+$", message = "Логин не должн содержать пробелы")
    String login;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    LocalDate birthday;
}
