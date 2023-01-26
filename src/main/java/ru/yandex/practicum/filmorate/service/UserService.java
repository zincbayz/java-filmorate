package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import ru.yandex.practicum.filmorate.exception_handler.ValidationException;
import ru.yandex.practicum.filmorate.model.user.User;
import ru.yandex.practicum.filmorate.dao.UserDao;
import ru.yandex.practicum.filmorate.exception_handler.RequiredObjectWasNotFound;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {

    private final UserDao userDao;

    @Autowired
    public UserService(@Qualifier("userDaoImpl")UserDao userDao) {
        this.userDao = userDao;
    }


    public User getUser(int id) {
        try {
            return userDao.getUser(id);
        } catch (EmptyResultDataAccessException e) {
            throw new RequiredObjectWasNotFound("User " + id + " not found");
        }
    }

    public List<User> getAllUsers() {
        return userDao.getAllUsers();
    }

    public List<User> getUsersFriends(int id) {
        return userDao.getUsersFriends(id);
    }

    public List<User> getCommonFriends(int id, int otherId) {
        List<User> commonFriends = userDao.getCommonFriends(id, otherId);
        return commonFriends.isEmpty() ? Collections.EMPTY_LIST : commonFriends;
    }


    public User create(User user) {
        return userDao.createUser(user);
    }

    public User update(User user) {
        try {
            return userDao.update(user, user.getId());
        } catch (EmptyResultDataAccessException e) {
            throw new RequiredObjectWasNotFound("User " + user.getId() + " not found");
        }
    }


    public void addFriend(int id, int friendId) {
        try {
            userDao.addFriend(id, friendId);
        } catch (EmptyResultDataAccessException e) {
            throw new ValidationException("User not found");
        }
        log.info("Users " + id + " and " + friendId + "have become friends");
    }

    public void deleteFriend(int id, int friendId) {
        userDao.deleteFriend(id, friendId);
        log.info("User " + id + " deleted user " + friendId + "from friends");
    }


}
