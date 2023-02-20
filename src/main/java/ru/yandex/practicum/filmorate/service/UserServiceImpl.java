package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.filmorate.controllers.UsersController;
import ru.yandex.practicum.filmorate.exception_handler.ValidationException;
import ru.yandex.practicum.filmorate.model.user.Feed;
import ru.yandex.practicum.filmorate.model.user.User;
import ru.yandex.practicum.filmorate.repository.FeedRepository;
import ru.yandex.practicum.filmorate.repository.UserRepository;
import ru.yandex.practicum.filmorate.exception_handler.RequiredObjectWasNotFound;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    FeedRepository feedRepository;

    @Autowired
    public UserServiceImpl(@Qualifier("userRepositoryImpl") UserRepository userRepository, FeedRepository feedRepository) {
        this.userRepository = userRepository;
        this.feedRepository = feedRepository;
    }


    @Override
    public User getUser(int id) {
        try {
            return userRepository.getUser(id);
        } catch (EmptyResultDataAccessException e) {
            throw new RequiredObjectWasNotFound("User " + id + " not found");
        }
    }

    @Override
    public List<User> getAllUsers() {
        return userRepository.getAllUsers();
    }

    @Override
    public List<User> getUsersFriends(int id) {
        return userRepository.getUsersFriends(id);
    }

    @Override
    public List<User> getCommonFriends(int id, int otherId) {
        List<User> commonFriends = userRepository.getCommonFriends(id, otherId);
        return commonFriends.isEmpty() ? Collections.EMPTY_LIST : commonFriends;
    }


    @Override
    public User create(User user) {
        return userRepository.createUser(user);
    }

    @Override
    public User update(User user) {
        try {
            return userRepository.update(user, user.getId());
        } catch (EmptyResultDataAccessException e) {
            throw new RequiredObjectWasNotFound("User " + user.getId() + " not found");
        }
    }


    @Override
    public void addFriend(int id, int friendId) {
        try {
            userRepository.addFriend(id, friendId);
            // добавляем в фид
            FeedRepository.createFeedEntity(id, friendId, "FRIEND", "ADD");
        } catch (EmptyResultDataAccessException e) {
            throw new ValidationException("User not found");
        }
        log.info("Users " + id + " and " + friendId + "have become friends");
    }

    @Override
    public void deleteFriend(int id, int friendId) {
        userRepository.deleteFriend(id, friendId);
        // добавляем в фид
        FeedRepository.createFeedEntity(id, friendId, "FRIEND", "REMOVE");
        log.info("User " + id + " deleted user " + friendId + "from friends");
    }

    /**
     * Возвращает ленту событий пользователя.
     **/
    public static List<Feed> findFeedByIdUser(int id) {
        try{
            return UsersController.findFeedByIdUser(id);
        } catch (EmptyResultDataAccessException e){
            throw new RequiredObjectWasNotFound("User " + id + " not found");
        }
    }

    private Feed makeFeed(ResultSet rs) throws SQLException {
        Feed feed = Feed.builder()
                .eventId(rs.getInt("EVENT_ID"))
                .userId(rs.getInt("USER_ID"))
                .entityId(rs.getInt("ENTITY_ID"))
                .eventType(rs.getString("EVENT_TYPE"))
                .operation(rs.getString("OPERATION"))
                .timestamp(rs.getLong("CREATE_TIME"))
                .build();

        if (feed == null) {
            return null;
        }
        return feed;
    }
}
