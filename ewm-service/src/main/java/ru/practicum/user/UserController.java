package ru.practicum.user;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import ru.practicum.exceptions.CreateValidation;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;

import java.util.List;

@Validated
@Slf4j
@RestController
@RequestMapping("/admin/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public UserDto addUser(@Validated(CreateValidation.class) @RequestBody NewUserRequest request) {
        log.info("Добавление нового пользователя");
        return userService.addUser(request);
    }

    @GetMapping
    public List<UserDto> findAllUsers(@RequestParam(required = false) List<Long> ids,
                                      @RequestParam(defaultValue = "0") Integer from,
                                      @RequestParam(defaultValue = "10") Integer size) {
        log.info("Запрошен список пользователей");
        return userService.findAllUsers(ids, from, size);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable("userId") Long userId) {
        log.info("Удаляется пользователь id={} ", userId);
        userService.deleteUser(userId);
        log.info("Пользователь с  id={} удален", userId);
    }

}
