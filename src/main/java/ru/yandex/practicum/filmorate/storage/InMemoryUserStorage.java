package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.User;

import java.util.*;
import java.util.stream.Collectors;

@Component
public class InMemoryUserStorage implements UserStorage {

    private final HashMap<Long, Set<Long>> usersFriends = new HashMap<>();
    private final HashMap<Long, User> users = new HashMap<>();
    private long currentId = 0L;

    @Override
    public User create(User user) {
        user.setId(getNextId());
        users.put(user.getId(), user);
        usersFriends.put(user.getId(), new HashSet<>());
        return user;
    }

    @Override
    public User update(User updateUser) {
        Long userId = updateUser.getId();

        if (!userExists(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }

        users.put(userId, updateUser);

        return updateUser;
    }

    @Override
    public Collection<User> findAll() {
        return  new ArrayList<>(users.values());
    }

    @Override
    public User getUserById(Long userId) {
        if (!userExists(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }

        return users.get(userId);
    }

    @Override
    public void addFriend(Long userId, Long friendUserId) {
        if (!userExists(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }

        if (!userExists(friendUserId)) {
            throw new NotFoundException("Пользователь которого нужно добавить в друзья не найден");
        }

        Set<Long> userFriends = usersFriends.get(userId);
        userFriends.add(friendUserId);
    }

    @Override
    public void deleteFriend(Long userId, Long friendUserId) {
        if (!userExists(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }

        if (!userExists(friendUserId)) {
            throw new NotFoundException("Пользователь которого нужно удалить из друзей не найден");
        }

        Set<Long> userFriends = usersFriends.get(userId);
        userFriends.remove(friendUserId);
    }

    @Override
    public Collection<User> findAllFriendsUser(Long userId) {
        if (!userExists(userId)) {
            throw new NotFoundException("Пользователь не найден");
        }

        return usersFriends.get(userId).stream()
                .map(users::get).collect(Collectors.toList());
    }

    private long getNextId() {
        return ++currentId;
    }

    private boolean userExists(Long id) {
        return users.containsKey(id);
    }
}
