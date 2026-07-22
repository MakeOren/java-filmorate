package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {

    private final UserStorage userStorage;
    private final FilmStorage filmStorage;
    private final int countPopularFilm = 10;

    public Film create(Film film) {
        validateFilm(film);

        Film newFilm = filmStorage.create(film);

        log.info("Создан фильм с id={}", newFilm.getId());
        return newFilm;
    }

    public Film update(Film updateFilm) {
        filmStorage.getFilmById(updateFilm.getId());

        validateUpdateFilm(updateFilm);

        Film newUpdateFilm = filmStorage.update(updateFilm);

        log.info("Обновлён фильм с id={}", updateFilm.getId());
        return newUpdateFilm;
    }

    public Collection<Film> findAll() {
        log.info("Вызван метод FilmService.findAll()");
        return new ArrayList<>(filmStorage.findAll());
    }

    public void addLike(Long filmId, Long userId) {
        if (filmId == null) {
            throw new ValidationException("Поле `id` не может быть пустым");
        }

        if (userId == null) {
            throw new ValidationException("Поле `userId` не может быть пустым");
        }

        userStorage.getUserById(userId);

        filmStorage.addLike(filmId, userId);
    }

    public void deleteLike(Long filmId, Long userId) {
        if (filmId == null) {
            throw new ValidationException("Поле `id` не может быть пустым");
        }

        if (userId == null) {
            throw new ValidationException("Поле `userId` не может быть пустым");
        }

        userStorage.getUserById(userId);

        filmStorage.deleteLike(filmId, userId);
    }

    public Collection<Film> getPopularFilms(Integer count) {

        if (count != null && count <= 0) {
            throw new ValidationException("Поле 'count' должно быть больше 0");
        } else if (count == null) {
            count = 10;
        }

        return new ArrayList<>(filmStorage.getPopularFilms(count));

       Map<Film, Integer> films = filmStorage.findAll()
               .stream()
               .collect(Collectors.toMap(film -> film, film -> filmStorage.findUsersLikeFilm(film.getId()).size()));

       return  films.keySet()
               .stream()
               .sorted(Comparator.comparing(films::get).reversed())
               .limit(count == null ? countPopularFilm : count)
               .collect(Collectors.toList());
    }

    public Film getFilmById(Long filmId) {
        return filmStorage.getFilmById(filmId);
    }

    private void validateFilm(Film film) {
        if (film == null) {
            throw new ValidationException("Тело запроса не может быть пустым");
        }

        validateNameAndDescriptionAndDurationAndReleaseDate(film);
    }

    private void validateUpdateFilm(Film updateFilm) {
        if (updateFilm == null) {
            throw new ValidationException("Тело запроса не может быть пустым");
        }

        if (updateFilm.getId() == null) {
            throw new ValidationException("Поле `id` не может быть пустым");
        }

        validateNameAndDescriptionAndDurationAndReleaseDate(updateFilm);
    }

    private void validateNameAndDescriptionAndDurationAndReleaseDate(Film film) {
        if (film.getName() == null || film.getName().isBlank()) {
            throw new ValidationException("Поле 'name' не может быть пустым");
        }

        if (film.getDescription() != null && film.getDescription().length() > 200) {
            throw new ValidationException("Поле 'description' не может больше 200");
        }

        if (film.getDuration() <= 0) {
            throw new ValidationException("Продолжительность должна быть положительным числом");
        }

        if (film.getReleaseDate() == null) {
            throw new ValidationException("Поле 'releaseDate' не может быть пустым");
        }
        if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
            throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
        }
    }
}
