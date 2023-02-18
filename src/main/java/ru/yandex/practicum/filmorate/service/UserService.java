package ru.yandex.practicum.filmorate.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception_handler.RequiredObjectWasNotFound;
import ru.yandex.practicum.filmorate.exception_handler.ValidationException;
import ru.yandex.practicum.filmorate.model.user.User;
import ru.yandex.practicum.filmorate.repository.UserRepository;

import java.util.Collections;
import java.util.List;

public interface UserService {

    User getUser(int id);

    List<User> getAllUsers();

    List<User> getUsersFriends(int id);

    List<User> getCommonFriends(int id, int otherId);

    User create(User user);

    User update(User user);

    void addFriend(int id, int friendId);

    void deleteFriend(int id, int friendId);


}
