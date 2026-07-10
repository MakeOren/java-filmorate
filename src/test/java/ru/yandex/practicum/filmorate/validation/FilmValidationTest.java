package ru.yandex.practicum.filmorate.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.FilmController;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.Film;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class FilmValidationTest {

    private Film film;
    private FilmController filmController;

    @BeforeEach
    void setUp() {
        film = new Film();
        film.setName("Valid Film");
        film.setDescription("Valid description");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(120);

        filmController = new FilmController();
    }

    //name
    @Test
    void shouldNotThrowExceptionWhenNameIsValid() {
        filmController.create(film);
        film.setId(1L);
        filmController.update(film);
    }

    @Test
    void shouldThrowExceptionWhenNameIsNull() {
        film.setName(null);
        assertThrows(ValidationException.class, () -> filmController.create(film));
        film.setId(1L);
        assertThrows(NotFoundException.class, () -> filmController.update(film));
    }

    @Test
    void shouldThrowExceptionWhenNameIsBlank() {
        film.setName("   ");
        assertThrows(ValidationException.class, () -> filmController.create(film));
        film.setId(1L);
        assertThrows(NotFoundException.class, () -> filmController.update(film));
    }

    //description
    @Test
    void shouldNotThrowExceptionWhenDescriptionIsNull() {
        film.setDescription(null);
        filmController.create(film);
        film.setId(1L);
        filmController.update(film);
    }

    @Test
    void shouldNotThrowExceptionWhenDescriptionLengthIsExactly200() {
        film.setDescription("a".repeat(200));
        filmController.create(film);
        film.setId(1L);
        filmController.update(film);
    }

    @Test
    void shouldThrowExceptionWhenDescriptionLengthIsMoreThan200() {
        film.setDescription("a".repeat(201));
        assertThrows(ValidationException.class, () -> filmController.create(film));
        film.setId(1L);
        assertThrows(NotFoundException.class, () -> filmController.update(film));
    }

    //duration
    @Test
    void shouldNotThrowExceptionWhenDurationIsPositive() {
        film.setDuration(100);
        filmController.create(film);
        film.setId(1L);
        filmController.update(film);
    }

    @Test
    void shouldThrowExceptionWhenDurationIsZero() {
        film.setDuration(0);
        assertThrows(ValidationException.class, () -> filmController.create(film));
        film.setId(1L);
        assertThrows(NotFoundException.class, () -> filmController.update(film));
    }

    @Test
    void shouldThrowExceptionWhenDurationIsNegative() {
        film.setDuration(-1);
        assertThrows(ValidationException.class, () -> filmController.create(film));
        film.setId(1L);
        assertThrows(NotFoundException.class, () -> filmController.update(film));
    }

    //release date
    @Test
    void shouldThrowExceptionWhenReleaseDateIsNull() {
        film.setReleaseDate(null);
        assertThrows(ValidationException.class, () -> filmController.create(film));

        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        filmController.create(film);

        film.setReleaseDate(null);
        assertThrows(ValidationException.class, () -> filmController.update(film));
    }

    @Test
    void shouldNotThrowExceptionWhenReleaseDateIsExactly28Dec1895() {
        film.setReleaseDate(LocalDate.of(1895, 12, 28));
        filmController.create(film);
        film.setId(1L);
        filmController.update(film);
    }

    @Test
    void shouldThrowExceptionWhenReleaseDateIsBefore28Dec1895() {
        film.setReleaseDate(LocalDate.of(1895, 12, 27));
        assertThrows(ValidationException.class, () -> filmController.create(film));
        film.setId(1L);
        assertThrows(NotFoundException.class, () -> filmController.update(film));
    }
}