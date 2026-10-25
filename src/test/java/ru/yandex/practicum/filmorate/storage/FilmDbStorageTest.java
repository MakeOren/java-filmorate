package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mappers.FilmRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;

import java.time.LocalDate;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({FilmDbStorage.class, FilmRowMapper.class, UserDbStorage.class, UserRowMapper.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class FilmDbStorageTest {

    private final FilmStorage filmStorage;
    private final UserStorage userStorage;

    @Test
    void createShouldSaveFilmWithMpaAndGenres() {
        Film film = filmStorage.create(newFilm("Фильм", 1, 1, 2));

        assertThat(film.getId()).isNotNull();
        assertThat(film.getMpa().getName()).isEqualTo("G");
        assertThat(film.getGenres())
                .extracting(Genre::getId)
                .containsExactly(1, 2);
        assertThat(film.getGenres().iterator().next().getName()).isEqualTo("Комедия");
    }

    @Test
    void createShouldWorkWithoutMpaAndGenres() {
        Film film = filmStorage.create(newFilm("Фильм", null));

        assertThat(film.getId()).isNotNull();
        assertThat(film.getMpa()).isNull();
        assertThat(film.getGenres()).isEmpty();
    }

    @Test
    void getFilmByIdShouldReturnEmptyForUnknownId() {
        assertThat(filmStorage.getFilmById(9999L)).isEmpty();
    }

    @Test
    void updateShouldChangeFieldsAndReplaceGenres() {
        Film film = filmStorage.create(newFilm("Старое", 1, 1, 2));
        Film changed = newFilm("Новое", 3, 3);
        changed.setId(film.getId());

        filmStorage.update(changed);

        assertThat(filmStorage.getFilmById(film.getId()))
                .hasValueSatisfying(found -> {
                    assertThat(found.getName()).isEqualTo("Новое");
                    assertThat(found.getMpa().getId()).isEqualTo(3);
                    assertThat(found.getGenres())
                            .extracting(Genre::getId)
                            .containsExactly(3);
                });
    }

    @Test
    void findAllShouldReturnFilmsEachWithOwnGenres() {
        Film first = filmStorage.create(newFilm("Первый", 1, 1));
        Film second = filmStorage.create(newFilm("Второй", 2, 2, 3));

        assertThat(filmStorage.findAll()).hasSize(2);
        Film foundFirst = filmStorage.findAll().stream()
                .filter(f -> f.getId().equals(first.getId())).findFirst().orElseThrow();
        Film foundSecond = filmStorage.findAll().stream()
                .filter(f -> f.getId().equals(second.getId())).findFirst().orElseThrow();
        assertThat(foundFirst.getGenres()).extracting(Genre::getId).containsExactly(1);
        assertThat(foundSecond.getGenres()).extracting(Genre::getId).containsExactly(2, 3);
    }

    @Test
    void getPopularFilmsShouldSortByLikesAndIncludeFilmsWithoutLikes() {
        Film noLikes = filmStorage.create(newFilm("Без лайков", 1));
        Film oneLike = filmStorage.create(newFilm("Один лайк", 1));
        Film twoLikes = filmStorage.create(newFilm("Два лайка", 1));
        User firstUser = userStorage.create(newUser("first"));
        User secondUser = userStorage.create(newUser("second"));

        filmStorage.addLike(twoLikes.getId(), firstUser.getId());
        filmStorage.addLike(twoLikes.getId(), secondUser.getId());
        filmStorage.addLike(oneLike.getId(), firstUser.getId());

        assertThat(filmStorage.getPopularFilms(10))
                .extracting(Film::getId)
                .containsExactly(twoLikes.getId(), oneLike.getId(), noLikes.getId());
        assertThat(filmStorage.getPopularFilms(2))
                .extracting(Film::getId)
                .containsExactly(twoLikes.getId(), oneLike.getId());
    }

    @Test
    void addLikeTwiceShouldCountOnce() {
        Film first = filmStorage.create(newFilm("Первый", 1));
        Film second = filmStorage.create(newFilm("Второй", 1));
        User user = userStorage.create(newUser("user"));
        User other = userStorage.create(newUser("other"));

        filmStorage.addLike(first.getId(), user.getId());
        filmStorage.addLike(first.getId(), user.getId());
        filmStorage.addLike(second.getId(), user.getId());
        filmStorage.addLike(second.getId(), other.getId());

        assertThat(filmStorage.getPopularFilms(1))
                .extracting(Film::getId)
                .containsExactly(second.getId());
    }

    @Test
    void deleteLikeShouldLowerFilmPosition() {
        Film first = filmStorage.create(newFilm("Первый", 1));
        Film second = filmStorage.create(newFilm("Второй", 1));
        User user = userStorage.create(newUser("user"));
        filmStorage.addLike(second.getId(), user.getId());

        filmStorage.deleteLike(second.getId(), user.getId());

        assertThat(filmStorage.getPopularFilms(10))
                .extracting(Film::getId)
                .containsExactly(first.getId(), second.getId());
    }

    private Film newFilm(String name, Integer mpaId, Integer... genreIds) {
        Film film = new Film();
        film.setName(name);
        film.setDescription("Описание");
        film.setReleaseDate(LocalDate.of(2000, 1, 1));
        film.setDuration(100);

        if (mpaId != null) {
            Mpa mpa = new Mpa();
            mpa.setId(mpaId);
            film.setMpa(mpa);
        }

        Set<Genre> genres = new LinkedHashSet<>();
        for (Integer genreId : genreIds) {
            Genre genre = new Genre();
            genre.setId(genreId);
            genres.add(genre);
        }
        film.setGenres(genres);
        return film;
    }

    private User newUser(String login) {
        User user = new User();
        user.setLogin(login);
        user.setEmail(login + "@mail.ru");
        user.setName("Имя " + login);
        user.setBirthday(LocalDate.of(1990, 5, 20));
        return user;
    }
}