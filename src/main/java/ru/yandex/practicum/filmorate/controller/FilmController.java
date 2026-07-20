package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.Film;
import ru.yandex.practicum.filmorate.service.FilmService;
import java.util.Collection;


@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/films")
public class FilmController {

    private final FilmService filmService;

    @PostMapping
    public Film create(@RequestBody Film film) {
        return filmService.create(film);
    }

    @PutMapping
    public Film update(@RequestBody Film updateFilm) {
        return filmService.update(updateFilm);
    }

    @GetMapping
    public Collection<Film> findAll() {
        return filmService.findAll();
    }

    @GetMapping("/{id}")
    public Film getFilm(@PathVariable long id) {
        return  filmService.getFilmById(id);
    }

    @PutMapping("/{id}/like/{userId}")
    public void addLike(@PathVariable long id,
                        @PathVariable long userId) {
        filmService.addLike(id, userId);
    }

    @DeleteMapping("/{id}/like/{userId}")
    public void deleteLike(@PathVariable long id,
                           @PathVariable long userId) {
        filmService.deleteLike(id, userId);
    }

    @GetMapping("/popular")
    public Collection<Film> getPopularFilm(@RequestParam(defaultValue = "10") int count) {
        return  filmService.getPopularFilms(count);
    }





}
