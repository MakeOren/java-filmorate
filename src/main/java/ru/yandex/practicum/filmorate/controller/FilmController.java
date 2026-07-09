package ru.yandex.practicum.filmorate.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;

@Slf4j
@RestController
@RequestMapping("/films")
public class FilmController {

    private final HashMap<Long, Film> films = new HashMap<>();
    private long currentId = 0L;

    @PostMapping
    public Film create(@RequestBody Film film) {
        long id = getNextId();

        validateFilm(film);
        film.setId(id);
        films.put(film.getId(), film);

        log.info("Создан фильм с id={}", film.getId());
        return film;
    }

    @PutMapping
    public Film update(@RequestBody Film updateFilm) {
        validateUpdateFilm(updateFilm);

        films.put(updateFilm.getId(), updateFilm);

        log.info("Обновлён фильм с id={}", updateFilm.getId());
        return updateFilm;
    }

    @GetMapping
    public Collection<Film> findAll() {
        return films.values();
    }

    private void validateFilm(Film film) {
        if (film == null) {
            throw new ValidationException("Тело запроса не может быть пустым");
        }

        if (film.getName() == null || film.getName().isBlank()) {
            throw new ValidationException("Поле 'name' не может быть пустым");
        }

        if (film.getDescription() != null) {
            if (film.getDescription().length() > 200) {
                throw new ValidationException("Поле 'description' не может больше 200");
            }
        }

        if (film.getDuration() <= 0) {
            throw new ValidationException("Продолжительность должна быть положительным числом");
        }

        if (film.getReleaseDate() != null) {
            if (film.getReleaseDate().isBefore(LocalDate.of(1895, 12, 28))) {
                throw new ValidationException("Дата релиза не может быть раньше 28 декабря 1895 года");
            }
        }
    }

    private long getNextId() {
        return ++currentId;
    }

    private void validateUpdateFilm(Film updateFilm) {
        if (updateFilm.getId() == 0) {
            throw new ValidationException("Поле `id` не может быть пустым");
        }

        Film film = films.get(updateFilm.getId());

        if (film == null) {
            throw new ValidationException("Фильм с данным `id` не найден");
        }

        validateFilm(updateFilm);
    }
}
