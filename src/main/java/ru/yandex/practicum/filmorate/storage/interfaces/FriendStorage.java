package ru.yandex.practicum.filmorate.storage.interfaces;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface FriendStorage {

    User addFriend(Long userId, Long friendId);

    User removeFriend(Long userId, Long friendId);

    Collection<User> getCommonFriends(Long userId, Long otherUserId);

    Collection<User> getFriends(Long userId);
}
