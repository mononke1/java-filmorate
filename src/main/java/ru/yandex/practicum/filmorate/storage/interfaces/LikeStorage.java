package ru.yandex.practicum.filmorate.storage.interfaces;

import ru.yandex.practicum.filmorate.model.Film;

public interface LikeStorage {

    Film addLike(Long filmId, Long userId);

    Film removeLike(Long filmId, Long userId);
}
