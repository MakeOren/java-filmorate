package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {

    User create(User user);
    User update(User updateUser);
    Collection<User> findAll();
    User getUserById(Long userId);
    void addFriend(Long userId, Long friendUserId);
    void deleteFriend(Long userId, Long friendUserId);
    Collection<User> findAllFriendsUser(Long userId);

}
