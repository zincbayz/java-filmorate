package ru.yandex.practicum.filmorate.repository;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.filmorate.util.FeedMapper;
import ru.yandex.practicum.filmorate.exception_handler.exceptions.RequiredObjectWasNotFound;
import ru.yandex.practicum.filmorate.util.FilmMapper;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.model.user.Feed;
import ru.yandex.practicum.filmorate.model.user.User;
import ru.yandex.practicum.filmorate.util.UserMapper;
import ru.yandex.practicum.filmorate.util.enums.EventType;
import ru.yandex.practicum.filmorate.util.enums.Operation;

import java.util.List;
@Slf4j
@Component
public class UserRepositoryImpl implements UserRepository {
    private final static int USER_DOESNT_EXIST = 0;

    private final JdbcTemplate jdbcTemplate;
    private final FilmRepositoryImpl filmRepository;

    @Autowired
    public UserRepositoryImpl(JdbcTemplate jdbcTemplate, FilmRepositoryImpl filmRepository) {
        this.jdbcTemplate = jdbcTemplate;
        this.filmRepository = filmRepository;
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
    public List<User> getUsersFriends(int userId) {
        final String sqlQuery = "SELECT * FROM USERS WHERE user_id IN (SELECT friend_id FROM Friends WHERE user_id=?)";
        return jdbcTemplate.query(sqlQuery, new UserMapper(), userId);
    }

    @Override
    public List<User> getCommonFriends(int id, int otherId) {
        final String sqlQuery = "SELECT * FROM USERS WHERE user_id IN (SELECT friend_id FROM Friends WHERE user_id=?) " +
                "INTERSECT SELECT * FROM USERS WHERE user_id IN (SELECT friend_id FROM Friends WHERE user_id=?)";
        return jdbcTemplate.query(sqlQuery, new UserMapper(), id, otherId);
    }

    @Override
    public List<Film> getRecommendations(int id) {
        final String ALL_FILMS_SQL_QUERY = "SELECT * FROM Films JOIN Mpa ON Films.mpa_id=Mpa.mpa_id ";
        final String getRecommendationFilms = ALL_FILMS_SQL_QUERY +
                "WHERE film_id IN (SELECT film_id FROM (SELECT user_id FROM Likes WHERE film_id in (SELECT film_id " +
                "FROM LIKES WHERE user_id = ?) AND user_id != ? GROUP BY user_id having MAX(film_id) > 1 ORDER BY " +
                "MAX(film_id) desc LIMIT 3) t1 JOIN (SELECT user_id, film_id FROM Likes) t2 ON t1.user_id = t2.user_id" +
                " where t2.film_id NOT IN (SELECT film_id FROM Likes Where user_id = ?))";

        List<Film> recommendationFilms = jdbcTemplate.query(getRecommendationFilms,
                new FilmMapper(), id, id, id);

        return filmRepository.addGenresInFilm(recommendationFilms);
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
        jdbcTemplate.update("INSERT INTO Friends (user_id, friend_id) VALUES (?, ?)", id, friendId);

    }

    @Override
    public void deleteFriend(int id, int friendId) {
        jdbcTemplate.update("DELETE FROM Friends WHERE user_id=? AND friend_id=?", id, friendId);
        jdbcTemplate.update("DELETE FROM Friends WHERE user_id=? AND friend_id=?", friendId, id);
    }

    @Override
    public void deleteUserById(int id) {
        jdbcTemplate.update("DELETE FROM users WHERE USER_ID = ?", id);
    }


    @Override
    public List<Feed> getFeed(int userId) {
        return jdbcTemplate.query("SELECT * FROM Feeds WHERE user_id=? " +
                        "UNION " +
                        "SELECT * FROM Feeds " +
                        "WHERE user_id IN (SELECT friend_id FROM Friends WHERE user_id=?) AND event_type LIKE '%REVIEW%'",
                new FeedMapper(),  userId, userId);
    }

    @Override
    public void insertFeed(int userId, EventType eventType, Operation operation, int entityId) {
        final String feedQuery = "INSERT INTO Feeds (user_id, event_type, operation, entity_id) VALUES (?, ?, ?, ?)";
        jdbcTemplate.update(feedQuery,
                userId, eventType.getEventType(), operation.getOperation(), entityId);
    }

    public void isUserExist(int userId) {
        int count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM Users where user_id=?",
                new Object[] { userId }, Integer.class);
        if (count == USER_DOESNT_EXIST) {
            throw new RequiredObjectWasNotFound("User not found");
        }
    }
}
