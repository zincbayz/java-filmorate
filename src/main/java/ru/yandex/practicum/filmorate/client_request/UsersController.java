package ru.yandex.practicum.filmorate.client_request;


import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.user.User;
import ru.yandex.practicum.filmorate.service.UserService;

import javax.validation.Valid;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/users")
public class UsersController {
    private final UserService userService;

    @Autowired
    public UsersController(UserService userService) {
        this.userService = userService;
    }


    @GetMapping("/{id}")
    public User getUser(@PathVariable(required = false) int id) {
        return userService.getUser(id);
    }

    @GetMapping()
    public List<User> getAllUsers() {
        return userService.getAllUsers();
    }

    @GetMapping("/{id}/friends")
    public List<User> getUsersFriends(@PathVariable int id) {
        return userService.getUsersFriends(id);
    }

    @GetMapping("/{id}/friends/common/{otherId}")
    public List<User> getCommonFriends(@PathVariable(required = false) int id, @PathVariable(required = false) int otherId) {
        return userService.getCommonFriends(id, otherId);
    }


    @PostMapping()
    public User createUser(@Valid @RequestBody User user) {
        return userService.create(buildUser(user));
    }

    @PutMapping()
    public User updateUser(@Valid @RequestBody User user) {
        return userService.update(buildUser(user));
    }

    @PutMapping("/{id}/friends/{friendId}")
    public void addFriend(@PathVariable int id, @PathVariable int friendId) {
        userService.addFriend(id, friendId);
    }


    @DeleteMapping("/{id}/friends/{friendId}")
    public void deleteFriend(@PathVariable int id, @PathVariable int friendId) {
        userService.deleteFriend(id, friendId);
    }



    private User buildUser(User user) {
        return User.builder()
                .id(user.getId())
                .email(user.getEmail())
                .login(user.getLogin())
                .name(user.getName())
                .birthday(user.getBirthday())
                .build();
    }
}
