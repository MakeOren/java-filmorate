package ru.yandex.practicum.filmorate.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.UserService;
import java.util.Collection;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    @PostMapping
    public User create(@RequestBody User user) {
        return userService.create(user);
    }

    @PutMapping
    public User update(@RequestBody User updateUser) {
        return userService.update(updateUser);
    }

    @GetMapping
    public Collection<User> findAll() {
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public User getUser(@PathVariable long id) {
        return userService.getUserById(id);
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable long id,
                          @PathVariable long friendId) {
        userService.addFriend(id,friendId);
    }

    @DeleteMapping("/{id}/friends/{friendId}")
    public void deleteFriend(@PathVariable long id,
                             @PathVariable long friendId) {
        userService.deleteFriend(id,friendId);
    }

    @GetMapping("/{id}/friends")
    public Collection<User> getFriendsUser(@PathVariable long id) {
        return userService.findAllFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public Collection<User> getCommonFriendsUsers(@PathVariable long id,
                                                  @PathVariable long otherId) {
        return userService.getCommonFriends(id, otherId);
    }




}
