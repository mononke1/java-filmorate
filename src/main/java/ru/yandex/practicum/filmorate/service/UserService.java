package ru.yandex.practicum.filmorate.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.interfaces.UserStorage;

import java.util.Collection;

@Service
public class UserService {
    @Autowired
    @Qualifier("userDbStorage")
    private UserStorage userStorage;

    public Collection<User> getAllUsers() {
        return userStorage.findAll();
    }

    public User getUserById(Long id) {
        return userStorage.findById(id);
    }

    public User createUser(User user) {
        return userStorage.create(user);
    }

    public User updateUser(User newUser) {
        return userStorage.update(newUser);
    }

    public User deleteUser(Long id) {
        return userStorage.delete(id);
    }
}
