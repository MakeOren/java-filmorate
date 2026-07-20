package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.Collection;

public interface UserStorage {

    User create(User user);

    /**
     * @throws NotFoundException если пользователь с данным id не найден
     */
    User update(User updateUser);

    Collection<User> findAll();

    /**
     * @throws NotFoundException если пользователь с данным id не найден
     */
    User getUserById(Long userId);

    /**
     * @throws NotFoundException если пользователи с данным id не найдены
     */
    void addFriend(Long userId, Long friendUserId);

    /**
     * @throws NotFoundException если пользователи с данными id не найдены
     */
    void deleteFriend(Long userId, Long friendUserId);

    /**
     * @throws NotFoundException если фильм с данным id не найден
     */
    Collection<User> findAllFriendsUser(Long userId);

}
