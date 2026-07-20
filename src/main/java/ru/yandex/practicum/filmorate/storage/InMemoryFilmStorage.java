package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Film;


import java.util.*;


@Component
public class InMemoryFilmStorage implements FilmStorage {

    private final HashMap<Long, Set<Long>> filmsLikeUsers = new HashMap<>();
    private final HashMap<Long, Film> films = new HashMap<>();
    private long currentId = 0L;


    @Override
    public Collection<Film> findAll() {
        return new ArrayList<>(films.values());
    }

    @Override
    public Collection<Long> findUsersLikeFilm(Long filmId) {
        if (!filmExits(filmId)) {
            throw new NotFoundException("Данный фильм не найден");
        }

        return  new ArrayList<>(filmsLikeUsers.get(filmId));
    }

    @Override
    public Film update(Film updateFilm) {
        if (!filmExits(updateFilm.getId())) {
            throw new NotFoundException("Данный фильм не найден");
        }

        films.put(updateFilm.getId(), updateFilm);
        return updateFilm;
    }

    @Override
    public Film create(Film film) {
        Long filmId = getNextId();
        film.setId(filmId);
        filmsLikeUsers.put(filmId, new HashSet<>());
        films.put(filmId, film);
        return film;
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        if (!filmExits(filmId)) {
            throw new NotFoundException("Фильм не найден");
        }

        Set<Long> filmLikeUsers = filmsLikeUsers.get(filmId);
        filmLikeUsers.add(userId);
    }

    @Override
    public void deleteLike(Long filmId, Long userId) {
        if (!filmExits(filmId)) {
            throw new NotFoundException("Фильм не найден");
        }

        Set<Long> filmLikeUsers = filmsLikeUsers.get(filmId);
        filmLikeUsers.remove(userId);
    }

    @Override
    public Film getFilmById(Long filmId) {
        if (!filmExits(filmId)) {
            throw new NotFoundException("Данный фильм не найден");
        }

        return films.get(filmId);
    }

    private boolean filmExits(Long id) {
        return films.containsKey(id);
    }

    private long getNextId() {
        return ++currentId;
    }
}
