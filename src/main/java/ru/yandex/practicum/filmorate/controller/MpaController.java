package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.filmorate.model.RatingMpa;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.util.List;

@RestController
@RequestMapping("/mpa")
@Slf4j
@RequiredArgsConstructor
public class MpaController {
    private final MpaService mpaService;

    @GetMapping
    public List<RatingMpa> getAllRatings() {
        log.info("Получен запрос на получение всех рейтингов MPA.");
        return mpaService.getAllRatings();
    }

    @GetMapping("/{id}")
    public RatingMpa getRatingById(@PathVariable int id) {
        log.info("Получен запрос на получение рейтинга MPA с ID {}", id);
        return mpaService.getRatingById(id);
    }
}
