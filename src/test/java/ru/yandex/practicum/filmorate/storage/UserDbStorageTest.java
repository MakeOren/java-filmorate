package ru.yandex.practicum.filmorate.storage;

import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.storage.mappers.UserRowMapper;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@AutoConfigureTestDatabase
@Import({UserDbStorage.class, UserRowMapper.class})
@RequiredArgsConstructor(onConstructor_ = @Autowired)
class UserDbStorageTest {

    private final UserStorage userStorage;

    @Test
    void createShouldAssignIdAndSaveFields() {
        User user = userStorage.create(newUser("ivan"));

        assertThat(user.getId()).isNotNull();
        assertThat(userStorage.getUserById(user.getId()))
                .hasValueSatisfying(found -> {
                    assertThat(found.getLogin()).isEqualTo("ivan");
                    assertThat(found.getEmail()).isEqualTo("ivan@mail.ru");
                    assertThat(found.getName()).isEqualTo("Имя ivan");
                    assertThat(found.getBirthday()).isEqualTo(LocalDate.of(1990, 5, 20));
                });
    }

    @Test
    void getUserByIdShouldReturnEmptyForUnknownId() {
        assertThat(userStorage.getUserById(9999L)).isEmpty();
    }

    @Test
    void updateShouldChangeFields() {
        User user = userStorage.create(newUser("ivan"));
        user.setName("Новое имя");
        user.setEmail("new@mail.ru");

        userStorage.update(user);

        assertThat(userStorage.getUserById(user.getId()))
                .hasValueSatisfying(found -> {
                    assertThat(found.getName()).isEqualTo("Новое имя");
                    assertThat(found.getEmail()).isEqualTo("new@mail.ru");
                });
    }

    @Test
    void findAllShouldReturnAllUsers() {
        userStorage.create(newUser("a"));
        userStorage.create(newUser("b"));

        assertThat(userStorage.findAll())
                .extracting(User::getLogin)
                .containsExactlyInAnyOrder("a", "b");
    }

    @Test
    void addFriendShouldBeOneWay() {
        User first = userStorage.create(newUser("first"));
        User second = userStorage.create(newUser("second"));

        userStorage.addFriend(first.getId(), second.getId());

        assertThat(userStorage.findAllFriendsUser(first.getId()))
                .extracting(User::getId)
                .containsExactly(second.getId());
        assertThat(userStorage.findAllFriendsUser(second.getId())).isEmpty();
    }

    @Test
    void addFriendTwiceShouldNotFail() {
        User first = userStorage.create(newUser("first"));
        User second = userStorage.create(newUser("second"));

        userStorage.addFriend(first.getId(), second.getId());
        userStorage.addFriend(first.getId(), second.getId());

        assertThat(userStorage.findAllFriendsUser(first.getId())).hasSize(1);
    }

    @Test
    void deleteFriendShouldRemoveOnlyOneDirection() {
        User first = userStorage.create(newUser("first"));
        User second = userStorage.create(newUser("second"));
        userStorage.addFriend(first.getId(), second.getId());
        userStorage.addFriend(second.getId(), first.getId());

        userStorage.deleteFriend(first.getId(), second.getId());

        assertThat(userStorage.findAllFriendsUser(first.getId())).isEmpty();
        assertThat(userStorage.findAllFriendsUser(second.getId())).hasSize(1);
    }

    @Test
    void existsByLoginShouldRespectExcludedId() {
        User user = userStorage.create(newUser("ivan"));

        assertThat(userStorage.existsByLogin("ivan", null)).isTrue();
        assertThat(userStorage.existsByLogin("ivan", user.getId())).isFalse();
        assertThat(userStorage.existsByLogin("other", null)).isFalse();
    }

    @Test
    void existsByEmailShouldRespectExcludedId() {
        User user = userStorage.create(newUser("ivan"));

        assertThat(userStorage.existsByEmail("ivan@mail.ru", null)).isTrue();
        assertThat(userStorage.existsByEmail("ivan@mail.ru", user.getId())).isFalse();
        assertThat(userStorage.existsByEmail("other@mail.ru", null)).isFalse();
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