package ru.yandex.practicum.filmorate.storage;

import org.springframework.stereotype.Component;
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

        users.put(userId, updateUser);

        return updateUser;
    }

    @Override
    public Collection<User> findAll() {
        return  new ArrayList<>(users.values());
    }

    @Override
    public Optional<User> getUserById(Long userId) {
        return Optional.ofNullable(users.get(userId));
    }

    @Override
    public void addFriend(Long userId, Long friendUserId) {
        Set<Long> userFriends = usersFriends.get(userId);
        userFriends.add(friendUserId);
    }

    @Override
    public void deleteFriend(Long userId, Long friendUserId) {
        Set<Long> userFriends = usersFriends.get(userId);
        userFriends.remove(friendUserId);
    }

    @Override
    public Collection<User> findAllFriendsUser(Long userId) {
        return usersFriends.get(userId).stream()
                .map(users::get).collect(Collectors.toList());
    }

    @Override
    public boolean existsByLogin(String login, Long excludeId) {
        return users.values()
                .stream()
                .filter(user1 -> excludeId == null || !user1.getId().equals(excludeId))
                .anyMatch(user -> user.getLogin().equals(login));
    }

    @Override
    public boolean existsByEmail(String email, Long excludeId) {
        return users.values()
                .stream()
                .filter(user1 -> excludeId == null || !user1.getId().equals(excludeId))
                .anyMatch(user -> user.getEmail().equals(email));
    }

    private long getNextId() {
        return ++currentId;
    }

}
