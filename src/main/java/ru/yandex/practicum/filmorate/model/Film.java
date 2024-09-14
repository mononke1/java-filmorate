package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;
import ru.yandex.practicum.filmorate.util.DateConstants;

import java.time.LocalDate;

@Data
public class Film {
    private final String FORMAT = DateConstants.FORMAT;
    private Long id;

    @NotBlank
    private String name;

    @Size(max = 200, message = "Максимальная длина описания — 200 символов")
    private String description;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = FORMAT)
    private LocalDate releaseDate;

    @Positive(message = "продолжительность фильма должна быть положительным числом")
    private Long duration;
}
