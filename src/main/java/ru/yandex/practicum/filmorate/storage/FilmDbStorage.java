package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowCallbackHandler;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.filmorate.exception.InternalServerException;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Repository
public class FilmDbStorage extends BaseDbStorage<Film> implements FilmStorage {

    private static final String SELECT_FILMS =
            "SELECT f.*, m.name AS mpa_name FROM films f LEFT JOIN mpa m ON m.id = f.mpa_id ";
    private static final String FIND_ALL = SELECT_FILMS + "ORDER BY f.id";
    private static final String FIND_BY_ID = SELECT_FILMS + "WHERE f.id = ?";
    private static final String FIND_POPULAR = SELECT_FILMS
            + "LEFT JOIN (SELECT film_id, COUNT(*) AS likes FROM film_likes GROUP BY film_id) l "
            + "ON l.film_id = f.id ORDER BY COALESCE(l.likes, 0) DESC, f.id LIMIT ?";

    private static final String INSERT_FILM =
            "INSERT INTO films (name, description, release_date, duration, mpa_id) VALUES (?, ?, ?, ?, ?)";
    private static final String UPDATE_FILM =
            "UPDATE films SET name = ?, description = ?, release_date = ?, duration = ?, mpa_id = ? WHERE id = ?";

    private static final String INSERT_FILM_GENRE = "INSERT INTO film_genres (film_id, genre_id) VALUES (?, ?)";
    private static final String DELETE_FILM_GENRES = "DELETE FROM film_genres WHERE film_id = ?";
    private static final String FIND_GENRES_START =
            "SELECT fg.film_id, g.id, g.name FROM film_genres fg JOIN genres g ON g.id = fg.genre_id "
                    + "WHERE fg.film_id IN (";
    private static final String FIND_GENRES_END = ") ORDER BY fg.film_id, g.id";

    private static final String ADD_LIKE =
            "MERGE INTO film_likes (film_id, user_id) KEY (film_id, user_id) VALUES (?, ?)";
    private static final String DELETE_LIKE = "DELETE FROM film_likes WHERE film_id = ? AND user_id = ?";

    public FilmDbStorage(JdbcTemplate jdbc, FilmRowMapper mapper) {
        super(jdbc, mapper);
    }

    @Override
    @Transactional
    public Film create(Film film) {
        long id = insert(
                INSERT_FILM,
                film.getName(),
                film.getDescription(),
                film.getReleaseDate(),
                film.getDuration(),
                getMpaId(film)
        );
        saveGenres(id, film.getGenres());
        return loadFilm(id);
    }

    @Override
    @Transactional
    public Film update(Film updateFilm) {
        update(
                UPDATE_FILM,
                updateFilm.getName(),
                updateFilm.getDescription(),
                updateFilm.getReleaseDate(),
                updateFilm.getDuration(),
                getMpaId(updateFilm),
                updateFilm.getId()
        );
        jdbc.update(DELETE_FILM_GENRES, updateFilm.getId());
        saveGenres(updateFilm.getId(), updateFilm.getGenres());
        return loadFilm(updateFilm.getId());
    }

    @Override
    public Collection<Film> findAll() {
        List<Film> films = findMany(FIND_ALL);
        loadGenres(films);
        return films;
    }

    @Override
    public Optional<Film> getFilmById(Long filmId) {
        Optional<Film> film = findOne(FIND_BY_ID, filmId);
        film.ifPresent(f -> loadGenres(List.of(f)));
        return film;
    }

    @Override
    public void addLike(Long filmId, Long userId) {
        jdbc.update(ADD_LIKE, filmId, userId);
    }

    @Override
    public void deleteLike(Long filmId, Long userId) {
        jdbc.update(DELETE_LIKE, filmId, userId);
    }

    @Override
    public Collection<Film> getPopularFilms(Integer count) {
        List<Film> films = findMany(FIND_POPULAR, count);
        loadGenres(films);
        return films;
    }

    private Film loadFilm(long id) {
        return getFilmById(id)
                .orElseThrow(() -> new InternalServerException("Не удалось загрузить фильм с id = " + id));
    }

    private Integer getMpaId(Film film) {
        return film.getMpa() == null ? null : film.getMpa().getId();
    }

    private void saveGenres(long filmId, Set<Genre> genres) {
        if (genres == null || genres.isEmpty()) {
            return;
        }
        List<Object[]> batch = genres.stream()
                .map(genre -> new Object[]{filmId, genre.getId()})
                .collect(Collectors.toList());
        jdbc.batchUpdate(INSERT_FILM_GENRE, batch);
    }

    private void loadGenres(Collection<Film> films) {
        if (films.isEmpty()) {
            return;
        }

        Map<Long, Film> filmsById = films.stream()
                .collect(Collectors.toMap(Film::getId, film -> film));
        String placeholders = films.stream()
                .map(film -> "?")
                .collect(Collectors.joining(", "));
        Object[] filmIds = filmsById.keySet().toArray();

        jdbc.query(FIND_GENRES_START + placeholders + FIND_GENRES_END, (RowCallbackHandler) rs -> {
            Genre genre = new Genre();
            genre.setId(rs.getInt("id"));
            genre.setName(rs.getString("name"));
            filmsById.get(rs.getLong("film_id")).getGenres().add(genre);
        }, filmIds);
    }
}
