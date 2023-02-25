package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.filmorate.exception_handler.exceptions.InternalServerError;
import ru.yandex.practicum.filmorate.model.user.Feed;
import ru.yandex.practicum.filmorate.model.film.Film;
import ru.yandex.practicum.filmorate.model.user.User;
import ru.yandex.practicum.filmorate.repository.UserRepository;
import ru.yandex.practicum.filmorate.exception_handler.exceptions.RequiredObjectWasNotFound;
import ru.yandex.practicum.filmorate.util.enums.EventType;
import ru.yandex.practicum.filmorate.util.enums.Operation;

import java.util.*;

@Slf4j
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
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
        userRepository.isUserExist(id);
        return userRepository.getUsersFriends(id);
    }

    public List<Film> getRecommendations(int id) {
        return userRepository.getRecommendations(id);
    }

    @Override
    public List<User> getCommonFriends(int id, int otherId) {
        List<User> commonFriends = userRepository.getCommonFriends(id, otherId);
        return commonFriends.isEmpty() ? Collections.EMPTY_LIST : commonFriends;
    }


    @Override
    public User create(User user) {
        return userRepository.createUser(buildUser(user));
    }

    @Override
    public User update(User user) {
        try {
            return userRepository.update(buildUser(user), user.getId());
        } catch (EmptyResultDataAccessException e) {
            throw new RequiredObjectWasNotFound("User " + user.getId() + " not found");
        }
    }


    @Override
    public void addFriend(int userId, int friendId) {
        try {
            userRepository.isUserExist(friendId);
            userRepository.addFriend(userId, friendId);
            userRepository.insertFeed(userId, EventType.FRIEND, Operation.ADD, friendId);
        } catch (EmptyResultDataAccessException e) {
            throw new InternalServerError("User not found");
        }
        log.info("Users " + userId + " and " + friendId + "have become friends");
    }

    @Override
    public void deleteFriend(int userId, int friendId) {
        userRepository.deleteFriend(userId, friendId);
        userRepository.insertFeed(userId, EventType.FRIEND, Operation.REMOVE, friendId);
        log.info("User " + userId + " deleted user " + friendId + "from friends");
    }

    @Override
    public void deleteUserById(int userId) {
        userRepository.deleteUserById(userId);
        log.info("Пользователь с id {} удален", userId);
    }

    @Override
    public List<Feed> getFeed(int userId) {
        userRepository.isUserExist(userId);
        return userRepository.getFeed(userId);
    }

    private User buildUser(User user) {
        user.setLogin(user.getLogin().trim());
        if (user.getName() == null || user.getName().isEmpty()) {
            user.setName(user.getLogin());
        }
        return user;
    }
}
