package ru.yandex.practicum.filmorate.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.exception_handler.exceptions.RequiredObjectWasNotFound;
import ru.yandex.practicum.filmorate.model.user.User;
import ru.yandex.practicum.filmorate.Util.UserMapper;

import java.util.List;
@Slf4j
@Component
public class UserRepositoryImpl implements UserRepository {

    private final JdbcTemplate jdbcTemplate;

    @Autowired
    public UserRepositoryImpl(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }


    @Override
    public User getUser(int id) throws EmptyResultDataAccessException {
        return jdbcTemplate.queryForObject("SELECT * FROM Users WHERE user_id=?",
                new UserMapper(), id);
    }

    @Override
    public List<User> getAllUsers() {
        return jdbcTemplate.query("SELECT * FROM Users", new UserMapper());
    }

    @Override
    public List<User> getUsersFriends(int id) {
        final String sqlQuery = "SELECT * FROM USERS WHERE user_id IN (SELECT friend_id FROM Friends WHERE user_id=?)";
        return jdbcTemplate.query(sqlQuery, new UserMapper(), id);
    }

    @Override
    public List<User> getCommonFriends(int id, int otherId) {
        final String sqlQuery = "SELECT * FROM USERS WHERE user_id IN (SELECT friend_id FROM Friends WHERE user_id=?) " +
                "INTERSECT SELECT * FROM USERS WHERE user_id IN (SELECT friend_id FROM Friends WHERE user_id=?)";
        return jdbcTemplate.query(sqlQuery, new UserMapper(), id, otherId);
    }


    @Override
    public User createUser(User user) {
        jdbcTemplate.update("INSERT INTO Users (email, login, name, birthday) " +
                        "VALUES (?, ?, ?, ?)",
                user.getEmail(),
                user.getLogin(),
                user.getName(),
                user.getBirthday());

        log.info("User created: " + user.getName());

        return jdbcTemplate.queryForObject("SELECT * FROM Users ORDER BY user_id DESC LIMIT 1",
                new UserMapper());
    }

    @Override
    public User update(User user, int userId) {
        final String sqlQuery = "UPDATE Users SET email=?, login=?, name=?, birthday=? WHERE user_id=?";
        jdbcTemplate.update(sqlQuery,
                user.getEmail(), user.getLogin(), user.getName(), user.getBirthday(), userId);
        log.info("User updated: " + user.getName());
        return getUser(userId);
    }

    @Override
    public void addFriend(int id, int friendId) throws EmptyResultDataAccessException{
        isRecordedUser(friendId);
        jdbcTemplate.update("INSERT INTO Friends (user_id, friend_id) VALUES (?, ?)", id, friendId);
    }

    @Override
    public void deleteFriend(int id, int friendId) {
        jdbcTemplate.update("DELETE FROM Friends WHERE user_id=? AND friend_id=?", id, friendId);
        jdbcTemplate.update("DELETE FROM Friends WHERE user_id=? AND friend_id=?", friendId, id);

    }

    private void isRecordedUser(int id) {
        try {
            getUser(id);
        } catch (EmptyResultDataAccessException e) {
            throw new RequiredObjectWasNotFound("User not found");
        }
    }
}
