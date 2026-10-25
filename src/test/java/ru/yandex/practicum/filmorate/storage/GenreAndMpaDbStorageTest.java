package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.Genre;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mappers.GenreRowMapper;
import ru.yandex.practicum.filmorate.storage.mappers.MpaRowMapper;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({GenreDbStorage.class, GenreRowMapper.class, MpaDbStorage.class, MpaRowMapper.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class GenreAndMpaDbStorageTest {

    private final GenreDbStorage genreStorage;
    private final MpaDbStorage mpaStorage;

    @Test
    void genreFindAllShouldReturnSixGenresSortedById() {
        assertThat(genreStorage.findAll())
                .extracting(Genre::getId)
                .containsExactly(1, 2, 3, 4, 5, 6);
    }

    @Test
    void genreFindByIdShouldReturnGenreOrEmpty() {
        assertThat(genreStorage.findById(1))
                .hasValueSatisfying(genre -> assertThat(genre.getName()).isEqualTo("Комедия"));
        assertThat(genreStorage.findById(999)).isEmpty();
    }

    @Test
    void mpaFindAllShouldReturnFiveRatingsSortedById() {
        assertThat(mpaStorage.findAll())
                .extracting(Mpa::getId)
                .containsExactly(1, 2, 3, 4, 5);
    }

    @Test
    void mpaFindByIdShouldReturnRatingOrEmpty() {
        assertThat(mpaStorage.findById(1))
                .hasValueSatisfying(mpa -> assertThat(mpa.getName()).isEqualTo("G"));
        assertThat(mpaStorage.findById(999)).isEmpty();
    }
}