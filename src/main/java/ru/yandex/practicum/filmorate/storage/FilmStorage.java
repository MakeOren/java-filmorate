package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;

import java.util.Collection;
import java.util.List;

public interface FilmStorage {

    Collection<Film> findAll();

    /**
     * @throws NotFoundException если фильм с данным id не найден
     */
    Film update(Film updateFilm);

    Film create(Film film);

    /**
     * @throws NotFoundException если фильм с данным id не найден
     */
    void addLike(Long filmId, Long userId);

    /**
     * @throws NotFoundException если фильм с данным id не найден
     */
    void deleteLike(Long filmId, Long userId);

    /**
     * @throws NotFoundException если фильм с данным id не найден
     */
    Film getFilmById(Long filmId);

    Collection<Long> findUsersLikeFilm(Long filmId);

    public Collection<List> getPopularFilms(Integer count);
}
