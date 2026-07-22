package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.storage.FilmStorage;
import ru.yandex.practicum.filmorate.storage.UserStorage;

import java.time.LocalDate;
import java.util.*;


@Service
@Slf4j
@RequiredArgsConstructor
public class FilmService {

    private final UserStorage userStorage;
    private final FilmStorage filmStorage;
    private final int defaultCountPopularFilm = 10;

    public Film create(Film film) {
        if (film == null) {
            throw new ValidationException("Тело запроса не может быть пустым");
        }

        validateNameAndDescriptionAndDurationAndReleaseDate(film);

        Film newFilm = filmStorage.create(film);

        log.info("Создан фильм с id={}", newFilm.getId());
        return newFilm;
    }

    public Film update(Film updateFilm) {
        if (updateFilm == null) {
            throw new ValidationException("Тело запроса не может быть пустым");
        }

        if (updateFilm.getId() == null) {
            throw new ValidationException("Поле `id` не может быть пустым");
        }

        if (filmStorage.getFilmById(updateFilm.getId()).isEmpty()) {
            throw new NotFoundException(String.format("Данный фильм c id = %d не найден", updateFilm.getId()));
        }

        validateNameAndDescriptionAndDurationAndReleaseDate(updateFilm);

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

        if (filmStorage.getFilmById(filmId).isEmpty()) {
            throw new NotFoundException(String.format("Данный фильм c id = %d не найден", filmId));
        }

        if (userStorage.getUserById(userId).isEmpty()) {
            throw new NotFoundException(String.format("Пользователь c данным id = %d не найден", userId));
        }

        filmStorage.addLike(filmId, userId);
    }

    public void deleteLike(Long filmId, Long userId) {
        if (filmId == null) {
            throw new ValidationException("Поле `id` не может быть пустым");
        }

        if (userId == null) {
            throw new ValidationException("Поле `userId` не может быть пустым");
        }

        if (filmStorage.getFilmById(filmId).isEmpty()) {
            throw new NotFoundException(String.format("Данный фильм c id = %d не найден", filmId));
        }

        if (userStorage.getUserById(userId).isEmpty()) {
            throw new NotFoundException(String.format("Пользователь c данным id = %d не найден", userId));
        }

        filmStorage.deleteLike(filmId, userId);
    }

    public Collection<Film> getPopularFilms(Integer count) {
        if (count != null && count <= 0) {
            throw new ValidationException("Поле 'count' должно быть больше 0");
        }

        int limit = (count == null) ? defaultCountPopularFilm : count;
        return new ArrayList<>(filmStorage.getPopularFilms(limit));
    }

    public Film getFilmById(Long filmId) {
        Optional<Film> filmOptional = filmStorage.getFilmById(filmId);

        if (filmOptional.isEmpty()) {
            throw new NotFoundException(String.format("Данный фильм c id = %d не найден", filmId));
        }

        return filmOptional.get();
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
