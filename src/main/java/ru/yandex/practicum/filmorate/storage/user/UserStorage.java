package ru.yandex.practicum.filmorate.storage.user;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {

    public Collection<User> findAll();

    public User create(User user);

    public User update(User newUser);

    public User delete(Long userId);

    public User findById(Long id);
}
