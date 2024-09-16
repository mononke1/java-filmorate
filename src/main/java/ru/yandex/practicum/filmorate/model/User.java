package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;
import ru.yandex.practicum.filmorate.util.DateUtil;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class User {
    private Set<Long> friends = new HashSet<>();
    private Long id;
    private String name;

    @NotEmpty(message = "Адрес электронной почты не может быть пустым")
    @Email(message = "Некорректный адрес электронной почты")
    private String email;

    @NotEmpty(message = "Логин не должен быть пустым")
    @Pattern(regexp = "^[^\\s]+$", message = "Логин не должн содержать пробелы")
    private String login;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateUtil.format)
    @PastOrPresent(message = "Дата рождения не может быть в будущем")
    private LocalDate birthday;
}
