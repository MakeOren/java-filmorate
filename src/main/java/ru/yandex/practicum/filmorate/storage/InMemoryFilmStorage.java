package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.model.Film;
import java.util.*;
import java.util.stream.Collectors;

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
    public Film update(Film updateFilm) {
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
        Set<Long> filmLikeUsers = filmsLikeUsers.get(filmId);
        filmLikeUsers.add(userId);
    }

    @Override
    public void deleteLike(Long filmId, Long userId) {
        Set<Long> filmLikeUsers = filmsLikeUsers.get(filmId);
        filmLikeUsers.remove(userId);
    }

    @Override
    public Optional<Film> getFilmById(Long filmId) {
        return Optional.ofNullable(films.get(filmId));
    }

    @Override
    public Collection<Film> getPopularFilms(Integer count) {
        return filmsLikeUsers.entrySet()
                .stream()
                .sorted(Comparator.comparingInt((Map.Entry<Long, Set<Long>> entry) -> entry.getValue().size())
                        .reversed())
                .limit(count)
                .map(entry -> films.get(entry.getKey()))
                .collect(Collectors.toList());
    }

    private long getNextId() {
        return ++currentId;
    }
}
