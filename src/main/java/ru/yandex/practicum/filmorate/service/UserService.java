package ru.yandex.practicum.filmorate.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

    private final UserStorage userStorage;

    public User create(User user) {
        validateUser(user);

        User newUser = userStorage.create(user);

        log.info("Создан пользователь с id={}", newUser.getId());
        return newUser;
    }


    public User update(User updateUser) {
        userStorage.getUserById(updateUser.getId());

        validateUpdateUser(updateUser);

        User newUpdateUser = userStorage.update(updateUser);

        log.info("Обновлён пользователь с id={}", newUpdateUser.getId());
        return newUpdateUser;
    }

    public Collection<User> findAllFriends(Long userId) {
        if (userId == null) {
            throw new ValidationException("Поле 'id' не может быть пустым");
        }

        log.info("Вызван метод UserService.findAllFriends()");
        return new ArrayList<>(userStorage.findAllFriendsUser(userId));
    }

    public Collection<User> findAll() {
        log.info("Вызван метод UserService.findAll()");
        return userStorage.findAll();
    }

    public void addFriend(Long userId1, Long userId2) {
        userStorage.addFriend(userId1, userId2);
        userStorage.addFriend(userId2, userId1);
        log.info("Пользователи с id={} и id={} добавлены в друзья",userId1, userId2);
    }

    public void deleteFriend(Long userId1, Long userId2) {
        userStorage.deleteFriend(userId1, userId2);
        userStorage.deleteFriend(userId2, userId1);
        log.info("Пользователи с id={} и id={} удалены из друзей",userId1, userId2);
    }

    public Collection<User> getCommonFriends(Long userId1, Long userId2) {
        Set<User> userFriends1 = new HashSet<>(userStorage.findAllFriendsUser(userId1));
        Set<User> userFriends2 = new HashSet<>(userStorage.findAllFriendsUser(userId2));
        userFriends1.retainAll(userFriends2);

        log.info("Вызван метод UserService.getCommonFriends()");

        return new ArrayList<>(userFriends1);
    }

    public User getUserById(Long userId) {
        return userStorage.getUserById(userId);
    }

    private void validateUser(User user) {
        if (user == null) {
            throw new ValidationException("Тело запроса не может быть пустым");
        }

        validateEmailAndLoginAndNameAndBirthday(user);

        checkEmailAndLoginUnique(user, null);

    }

    private void validateUpdateUser(User updateUser) {
        if (updateUser == null) {
            throw new ValidationException("Тело запроса не может быть пустым");
        }

        if (updateUser.getId() == null) {
            throw new ValidationException("Поле `id` не может быть пустым");
        }

        validateEmailAndLoginAndNameAndBirthday(updateUser);

        checkEmailAndLoginUnique(updateUser, updateUser.getId());
    }

    private void checkEmailAndLoginUnique(User user, Long excludeId) {
        List<User> users = new ArrayList<>(userStorage.findAll());

        boolean loginExists = users
                .stream()
                .filter(user1 -> excludeId == null || !user1.getId().equals(excludeId))
                .anyMatch(user1 -> user1.getLogin().equals(user.getLogin()));

        boolean emailExists = users
                .stream()
                .filter(user1 -> excludeId == null || !user1.getId().equals(excludeId))
                .anyMatch(user1 -> user1.getEmail().equals(user.getEmail()));

        if (emailExists) {
            throw new ValidationException("Пользователь с таким email уже существует");
        }

        if (loginExists) {
            throw new ValidationException("Пользователь с таким login уже существует");
        }
    }

    private void validateEmailAndLoginAndNameAndBirthday(User user) {
        if (user.getEmail() == null || user.getEmail().isBlank())  {
            throw new ValidationException("Поле 'email' не может быть пустым");
        }

        if (!user.getEmail().contains("@")) {
            throw new ValidationException("Некорректный email");
        }

        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            throw new ValidationException("Поле 'login' не может быть пустым");
        }


        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        if (user.getBirthday() == null) {
            throw new ValidationException("Поле 'birthday' не может быть пустым");
        }

        if (user.getBirthday().isAfter(LocalDate.now())) {
            throw new ValidationException("Дата рождения не может быть в будущем");
        }
    }

}
