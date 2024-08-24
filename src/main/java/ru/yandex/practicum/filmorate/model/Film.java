package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class Film {
    Long id;
    String name;

    @Size(max = 200, message = "Максимальная длина описания — 200 символов")
    String description;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    LocalDate releaseDate;

    @Positive(message = "продолжительность фильма должна быть положительным числом")
    Long duration;
}
