package ru.yandex.practicum.filmorate.storage;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;

import java.util.Collection;
import java.util.Optional;

@Repository
public class UserDbStorage extends BaseDbStorage<User> implements UserStorage {

    private static final String INSERT_USER =
            "INSERT INTO users (email, login, name, birthday) VALUES (?, ?, ?, ?)";
    private static final String UPDATE_USER =
            "UPDATE users SET email = ?, login = ?, name = ?, birthday = ? WHERE id = ?";
    private static final String FIND_ALL = "SELECT * FROM users";
    private static final String FIND_BY_ID = "SELECT * FROM users WHERE id = ?";
    private static final String ADD_FRIEND =
            "MERGE INTO friendships (user_id, friend_id) KEY (user_id, friend_id) VALUES (?, ?)";
    private static final String DELETE_FRIEND =
            "DELETE FROM friendships WHERE user_id = ? AND friend_id = ?";
    private static final String FIND_FRIENDS =
            "SELECT u.* FROM users u JOIN friendships f ON u.id = f.friend_id WHERE f.user_id = ?";
    private static final String COUNT_BY_LOGIN = "SELECT COUNT(*) FROM users WHERE login = ?";
    private static final String COUNT_BY_LOGIN_EXCLUDING =
            "SELECT COUNT(*) FROM users WHERE login = ? AND id <> ?";
    private static final String COUNT_BY_EMAIL = "SELECT COUNT(*) FROM users WHERE email = ?";
    private static final String COUNT_BY_EMAIL_EXCLUDING =
            "SELECT COUNT(*) FROM users WHERE email = ? AND id <> ?";

    public UserDbStorage(JdbcTemplate jdbc, UserRowMapper mapper) {
        super(jdbc, mapper);
    }

    @Override
    public User create(User user) {
        long id = insert(
                INSERT_USER,
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday()
        );
        user.setId(id);
        return user;
    }

    @Override
    public User update(User updateUser) {
        update(
                UPDATE_USER,
                updateUser.getEmail(),
                updateUser.getLogin(),
                updateUser.getName(),
                updateUser.getBirthday(),
                updateUser.getId()
        );
        return updateUser;
    }

    @Override
    public Collection<User> findAll() {
        return findMany(FIND_ALL);
    }

    @Override
    public Optional<User> getUserById(Long userId) {
        return findOne(FIND_BY_ID, userId);
    }

    @Override
    public void addFriend(Long userId, Long friendUserId) {
        jdbc.update(ADD_FRIEND, userId, friendUserId);
    }

    @Override
    public void deleteFriend(Long userId, Long friendUserId) {
        jdbc.update(DELETE_FRIEND, userId, friendUserId);
    }

    @Override
    public Collection<User> findAllFriendsUser(Long userId) {
        return findMany(FIND_FRIENDS, userId);
    }

    @Override
    public boolean existsByLogin(String login, Long excludeId) {
        if (excludeId == null) {
            return count(COUNT_BY_LOGIN, login) > 0;
        }
        return count(COUNT_BY_LOGIN_EXCLUDING, login, excludeId) > 0;
    }

    @Override
    public boolean existsByEmail(String email, Long excludeId) {
        if (excludeId == null) {
            return count(COUNT_BY_EMAIL, email) > 0;
        }
        return count(COUNT_BY_EMAIL_EXCLUDING, email, excludeId) > 0;
    }

    private int count(String query, Object... params) {
        Integer result = jdbc.queryForObject(query, Integer.class, params);
        return result == null ? 0 : result;
    }
}
