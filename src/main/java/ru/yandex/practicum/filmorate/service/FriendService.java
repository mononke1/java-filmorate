package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.interfaces.FriendStorage;

import java.util.Collection;

@Service
@Slf4j
public class FriendService {

    private final FriendStorage friendStorage;

    @Autowired
    public FriendService(@Qualifier("friendDbStorage") FriendStorage friendStorage) {
        this.friendStorage = friendStorage;
    }

    public User addFriend(Long userId, Long friendId) {
        return friendStorage.addFriend(userId, friendId);
    }

    public User removeFriend(Long userId, Long friendId) {
        return friendStorage.removeFriend(userId, friendId);
    }

    public Collection<User> getCommonFriends(Long userId, Long otherUserId) {
        return friendStorage.getCommonFriends(userId, otherUserId);
    }

    public Collection<User> getFriends(Long userId) {
        return friendStorage.getFriends(userId);
    }
}
