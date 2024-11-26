package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.RatingMpa;
import ru.yandex.practicum.filmorate.storage.db.MpaDbStorage;

import java.util.List;

@Service
public class MpaService {
    @Autowired
    private MpaDbStorage mpaDbStorage;

    public List<RatingMpa> getAllRatings() {
        return mpaDbStorage.findAll();
    }

    public RatingMpa getRatingById(int id) {
        return mpaDbStorage.findById(id);
    }
}
