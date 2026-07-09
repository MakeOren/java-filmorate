package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;

@Slf4j
@RestController
@RequestMapping("/users")
public class UserController {
    private final HashMap<Long, User> users = new HashMap<>();
    private long currentId = 0L;

    @PostMapping
    public User create(@RequestBody User user) {
        long id = getNextId();

        validateUser(user);
        user.setId(id);
        users.put(user.getId(), user);

        log.info("Создан пользователь с id={}", user.getId());
        return user;
    }

    @PutMapping
    public User update(@RequestBody User updateUser) {
        validateUpdateUser(updateUser);

        users.put(updateUser.getId(), updateUser);

        log.info("Обновлён пользователь с id={}", updateUser.getId());
        return updateUser;
    }

    @GetMapping
    public Collection<User> findAll() {
        return users.values();
    }

    private void validateUser(User user) {
        if (user == null) {
            throw new ValidationException("Тело запроса не может быть пустым");
        }

        if (user.getEmail() == null || user.getEmail().isBlank())  {
            throw new ValidationException("Поле 'email' не может быть пустым");
        }

        if (!user.getEmail().contains("@")) {
            throw new ValidationException("Некорректный email");
        }

        if (user.getLogin() == null || user.getLogin().isBlank() || user.getLogin().contains(" ")) {
            throw new ValidationException("Поле 'login' не может быть пустым");
        }

        boolean loginExists = users.values()
                .stream()
                .filter(user1 -> user1.getId() != user.getId())
                .anyMatch(user1 -> user1.getLogin().equals(user.getLogin()));

        boolean emailExists = users.values()
                .stream()
                .filter(user1 -> user1.getId() != user.getId())
                .anyMatch(user1 -> user1.getEmail().equals(user.getEmail()));

        if (emailExists) {
            throw new ValidationException("Пользователь с таким email уже существует");
        }

        if (loginExists) {
            throw new ValidationException("Пользователь с таким login уже существует");
        }

        if (user.getName() == null || user.getName().isBlank()) {
            user.setName(user.getLogin());
        }

        if (user.getBirthday() != null) {
            if (user.getBirthday().isAfter(LocalDate.now())) {
                throw new ValidationException("Дата рождения не может быть в будущем");
            }
        }


    }

    private long getNextId() {
        return ++currentId;
    }

    private void validateUpdateUser(User updateUser) {
        if (updateUser.getId() == 0) {
            throw new ValidationException("Поле `id` не может быть пустым");
        }

        User user = users.get(updateUser.getId());

        if (user == null) {
            throw new NotFoundException("Пользователь с данным `id` не найден");
        }

        validateUser(updateUser);
    }
}
