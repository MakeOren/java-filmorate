package ru.yandex.practicum.filmorate.service;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
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
        if (updateUser == null) {
            throw new ValidationException("Тело запроса не может быть пустым");
        }

        if (updateUser.getId() == null) {
            throw new ValidationException("Поле `id` не может быть пустым");
        }

        if (userStorage.getUserById(updateUser.getId()).isEmpty()) {
            throw new NotFoundException(String.format("Пользователь c данным id = %d не найден", updateUser.getId()));
        }

        validateUpdateUser(updateUser);

        User newUpdateUser = userStorage.update(updateUser);

        log.info("Обновлён пользователь с id={}", newUpdateUser.getId());
        return newUpdateUser;
    }

    public Collection<User> findAllFriends(Long userId) {
        if (userId == null) {
            throw new ValidationException("Поле 'id' не может быть пустым");
        }

        if (userStorage.getUserById(userId).isEmpty()) {
            throw new NotFoundException(String.format("Пользователь c данным id = %d не найден", userId));
        }

        log.info("Вызван метод UserService.findAllFriends()");
        return new ArrayList<>(userStorage.findAllFriendsUser(userId));
    }

    public Collection<User> findAll() {
        log.info("Вызван метод UserService.findAll()");
        return userStorage.findAll();
    }

    public void addFriend(Long userId1, Long userId2) {
        if (userStorage.getUserById(userId1).isEmpty()) {
            throw new NotFoundException(String.format("Пользователь c данным id = %d не найден", userId1));
        }

        if (userStorage.getUserById(userId2).isEmpty()) {
            throw new NotFoundException(String.format("Пользователь которого нужно добавить в друзья c данным id = %d не найден", userId2));
        }

        if (userId1.equals(userId2)) {
            throw new ValidationException("Нельзя добавить самого себя в друзья");
        }

        userStorage.addFriend(userId1, userId2);
        userStorage.addFriend(userId2, userId1);
        log.info("Пользователи с id={} и id={} добавлены в друзья",userId1, userId2);
    }

    public void deleteFriend(Long userId1, Long userId2) {
        if (userStorage.getUserById(userId1).isEmpty()) {
            throw new NotFoundException(String.format("Пользователь c данным id = %d не найден", userId1));
        }

        if (userStorage.getUserById(userId2).isEmpty()) {
            throw new NotFoundException(String.format("Пользователь которого нужно удалить из друзей c данным id = %d не найден", userId2));
        }

        if (userId1.equals(userId2)) {
            throw new ValidationException("Нельзя добавить самого себя в друзья");
        }

        userStorage.deleteFriend(userId1, userId2);
        userStorage.deleteFriend(userId2, userId1);
        log.info("Пользователи с id={} и id={} удалены из друзей",userId1, userId2);
    }

    public Collection<User> getCommonFriends(Long userId1, Long userId2) {
        if (userStorage.getUserById(userId1).isEmpty()) {
            throw new NotFoundException(String.format("Пользователь c данным id = %d не найден", userId1));
        }

        if (userStorage.getUserById(userId2).isEmpty()) {
            throw new NotFoundException(String.format("Пользователь c данным id = %d не найден", userId2));
        }

        if (userId1.equals(userId2)) {
            throw new ValidationException("Нельзя добавить самого себя в друзья");
        }

        Set<User> userFriends1 = new HashSet<>(userStorage.findAllFriendsUser(userId1));
        Set<User> userFriends2 = new HashSet<>(userStorage.findAllFriendsUser(userId2));
        userFriends1.retainAll(userFriends2);

        log.info("Вызван метод UserService.getCommonFriends()");

        return new ArrayList<>(userFriends1);
    }

    public User getUserById(Long userId) {
        Optional<User> userOptional = userStorage.getUserById(userId);

        if (userOptional.isEmpty()) {
            throw new NotFoundException("Пользователь не найден");
        }

        return userOptional.get();
    }

    private void validateUser(User user) {
        if (user == null) {
            throw new ValidationException("Тело запроса не может быть пустым");
        }

        validateEmailAndLoginAndNameAndBirthday(user);

        if (userStorage.existsByEmail(user.getEmail(), null)) {
            throw new ValidationException("Пользователь с таким email уже существует");
        }

        if (userStorage.existsByLogin(user.getLogin(), null)) {
            throw new ValidationException("Пользователь с таким login уже существует");
        }

    }

    private void validateUpdateUser(User updateUser) {
        validateEmailAndLoginAndNameAndBirthday(updateUser);

        if (userStorage.existsByEmail(updateUser.getEmail(), updateUser.getId())) {
            throw new ValidationException("Пользователь с таким email уже существует");
        }

        if (userStorage.existsByLogin(updateUser.getLogin(), updateUser.getId())) {
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
