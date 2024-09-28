package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.*;
import lombok.Data;
import ru.yandex.practicum.filmorate.util.DateUtil;

import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

@Data
public class Film {
    private Long id;
    private Set<Long> likes = new HashSet<>();

    @NotBlank(message = "название не должно быть пустым")
    private String name;

    @Size(max = 200, message = "Максимальная длина описания — 200 символов")
    private String description;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = DateUtil.FORMAT)
    @PastOrPresent(message = "Фильм должен уже быть доступен для просмотра")
    private LocalDate releaseDate;

    @Positive(message = "продолжительность фильма должна быть положительным числом")
    private Long duration;
}
