package ru.yandex.practicum.filmorate.storage;

import ru.yandex.practicum.filmorate.model.Film;
import java.util.Collection;
import java.util.Optional;

public interface FilmStorage {

    Collection<Film> findAll();

    Film update(Film updateFilm);

    Film create(Film film);

    void addLike(Long filmId, Long userId);

    void deleteLike(Long filmId, Long userId);

    Optional<Film> getFilmById(Long filmId);

    public Collection<Film> getPopularFilms(Integer count);
}
