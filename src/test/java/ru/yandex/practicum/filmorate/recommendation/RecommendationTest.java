package ru.yandex.practicum.filmorate.recommendation;

import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryFilmStorage;
import ru.yandex.practicum.filmorate.storage.InMemoryUserStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RecommendationTest {

    @Test
    void shouldRecommendFilmsLikedBySimilarUser() {
        FilmStorage filmStorage = new InMemoryFilmStorage();
        UserStorage userStorage = new InMemoryUserStorage();
        UserService userService = new UserService(userStorage, filmStorage);

        Film film1 = createFilm("Фильм 1");
        Film film2 = createFilm("Фильм 2");
        Film film3 = createFilm("Фильм 3");
        Film film4 = createFilm("Фильм 4");

        filmStorage.create(film1);
        filmStorage.create(film2);
        filmStorage.create(film3);
        filmStorage.create(film4);

        User user1 = createUser("user1");
        User user2 = createUser("user2");
        User user3 = createUser("user3");

        userStorage.create(user1);
        userStorage.create(user2);
        userStorage.create(user3);

        filmStorage.addLike(film1.getId(), user1.getId());
        filmStorage.addLike(film2.getId(), user1.getId());

        filmStorage.addLike(film1.getId(), user2.getId());
        filmStorage.addLike(film2.getId(), user2.getId());
        filmStorage.addLike(film3.getId(), user2.getId());
        filmStorage.addLike(film4.getId(), user2.getId());

        filmStorage.addLike(film1.getId(), user3.getId());

        Collection<Film> recommendations = userService.getRecommendations(user1.getId());

        assertEquals(
                2,
                recommendations.size(),
                "Количество рекомендаций должно быть равно двум"
        );

        assertTrue(
                recommendations.stream()
                        .anyMatch(film -> film.getId().equals(film3.getId())),
                "Третий фильм должен быть в рекомендациях"
        );

        assertTrue(
                recommendations.stream()
                        .anyMatch(film -> film.getId().equals(film4.getId())),
                "Четвёртый фильм должен быть в рекомендациях"
        );

        assertTrue(
                recommendations.stream()
                        .noneMatch(film -> film.getId().equals(film1.getId())),
                "Первый фильм не должен быть в рекомендациях"
        );

        assertTrue(
                recommendations.stream()
                        .noneMatch(film -> film.getId().equals(film2.getId())),
                "Второй фильм не должен быть в рекомендациях"
        );
    }

    private Film createFilm(String name) {
        Film film = new Film();
        film.setName(name);
        film.setDescription("Тестовое описание");
        film.setReleaseDate(LocalDate.of(2020, 1, 1));
        film.setDuration(120);
        return film;
    }

    private User createUser(String login) {
        User user = new User();
        user.setEmail(login + "@test.com");
        user.setLogin(login);
        user.setName(login);
        user.setBirthday(LocalDate.of(2000, 1, 1));
        return user;
    }
}
