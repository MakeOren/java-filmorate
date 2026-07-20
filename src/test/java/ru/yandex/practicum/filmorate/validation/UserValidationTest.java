package ru.yandex.practicum.filmorate.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

public class UserValidationTest {

    User user;
    UserController userController;
    UserStorage userStorage;
    UserService userService;

    @BeforeEach
    void setUp() {
        user = new User();
        user.setEmail("ruslan@gmail.com");
        user.setLogin("makeoren");
        user.setBirthday(LocalDate.of(2002, 5, 7));
        user.setName("Руслан");

        userStorage = new InMemoryUserStorage();

        userService = new UserService(userStorage);

        userController = new UserController(userService);
    }

    //email
    @Test
    void shouldNotThrowExceptionWhenEmailIsValid() {
        userController.create(user);
        userController.update(user);
    }

    @Test
    void shouldThrowExceptionWhenEmailIsNull() {
        user.setEmail(null);
        assertThrows(ValidationException.class, () -> userController.create(user));
        user.setId((long) 1);
        assertThrows(NotFoundException.class, () -> userController.update(user));
    }

    @Test
    void shouldThrowExceptionWhenEmailIsBlank() {
        user.setEmail("   ");
        assertThrows(ValidationException.class, () -> userController.create(user));
        user.setId((long) 1);
        assertThrows(NotFoundException.class, () -> userController.update(user));
    }

    @Test
    void shouldThrowExceptionWhenEmailDoesNotContainAtSymbol() {
        user.setEmail("hyizenberg");
        assertThrows(ValidationException.class, () -> userController.create(user));
        user.setId((long) 1);
        assertThrows(NotFoundException.class, () -> userController.update(user));
    }

    @Test
    void shouldNotThrowExceptionWhenLoginIsValid() {
        userController.create(user);
        user.setId(1L);
        userController.update(user);
    }

    //login
    @Test
    void shouldThrowExceptionWhenLoginIsNull() {
        user.setLogin(null);
        assertThrows(ValidationException.class, () -> userController.create(user));
        user.setId(1L);
        assertThrows(NotFoundException.class, () -> userController.update(user));
    }

    @Test
    void shouldThrowExceptionWhenLoginIsBlank() {
        user.setLogin("   ");
        assertThrows(ValidationException.class, () -> userController.create(user));
        user.setId(1L);
        assertThrows(NotFoundException.class, () -> userController.update(user));
    }

    @Test
    void shouldThrowExceptionWhenLoginContainsSpaces() {
        user.setLogin("invalid login");
        assertThrows(ValidationException.class, () -> userController.create(user));
        user.setId(1L);
        assertThrows(NotFoundException.class, () -> userController.update(user));
    }

    //name
    @Test
    void shouldSetNameToLoginWhenNameIsNull() {
        user.setName(null);
        userController.create(user);
        assertEquals("makeoren", user.getName());
    }

    @Test
    void shouldSetNameToLoginWhenNameIsBlank() {
        user.setName("");
        userController.create(user);
        assertEquals("makeoren", user.getName());
    }

    @Test
    void shouldNotThrowExceptionWhenNameIsValid() {
        user.setName("Руслан");
        userController.create(user);
        user.setId(1L);
        userController.update(user);
    }

    //birthday
    @Test
    void shouldThrowExceptionWhenBirthdayIsNull() {
        user.setBirthday(null);
        assertThrows(ValidationException.class, () -> userController.create(user));

        user.setBirthday(LocalDate.of(2002, 5, 7));
        userController.create(user);

        user.setBirthday(null);
        assertThrows(ValidationException.class, () -> userController.update(user));
    }

    @Test
    void shouldNotThrowExceptionWhenBirthdayIsToday() {
        user.setBirthday(LocalDate.now());
        userController.create(user);
        user.setId(1L);
        userController.update(user);
    }

    @Test
    void shouldNotThrowExceptionWhenBirthdayIsInPast() {
        user.setBirthday(LocalDate.of(2000, 1, 1));
        userController.create(user);
        user.setId(1L);
        userController.update(user);
    }

    @Test
    void shouldThrowExceptionWhenBirthdayIsInFuture() {
        user.setBirthday(LocalDate.now().plusDays(1));
        assertThrows(ValidationException.class, () -> userController.create(user));
        user.setId(1L);
        assertThrows(NotFoundException.class, () -> userController.update(user));
    }

    @Test
    void shouldThrowExceptionWhenUserNotFound() {
        user.setId(1L);
        assertThrows(NotFoundException.class, () -> userController.update(user));
    }
}
