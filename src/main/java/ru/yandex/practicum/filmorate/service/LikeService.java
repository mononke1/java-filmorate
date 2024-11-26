package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.interfaces.LikeStorage;

@Service
public class LikeService {
    @Autowired
    @Qualifier("likeDbStorage")
    private LikeStorage likeStorage;

    public Film addLike(Long filmId, Long userId) {
        return likeStorage.addLike(filmId, userId);
    }

    public Film removeLike(Long filmId, Long userId) {
        return likeStorage.removeLike(filmId, userId);
    }
}
