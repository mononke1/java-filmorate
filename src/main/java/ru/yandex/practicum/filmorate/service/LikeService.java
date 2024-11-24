package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.interfaces.LikeStorage;

@Service
@Slf4j
public class LikeService {

    private final LikeStorage likeStorage;

    @Autowired
    public LikeService(@Qualifier("likeDbStorage") LikeStorage likeStorage) {
        this.likeStorage = likeStorage;
    }

    public Film addLike(Long filmId, Long userId) {
        return likeStorage.addLike(filmId, userId);
    }

    public Film removeLike(Long filmId, Long userId) {
        return likeStorage.removeLike(filmId, userId);
    }
}
