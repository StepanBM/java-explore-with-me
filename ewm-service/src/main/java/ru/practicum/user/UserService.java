package ru.practicum.user;

import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;

import java.util.List;

public interface UserService {

    UserDto addUser(NewUserRequest request);

    List<UserDto> findAllUsers(List<Long> ids, Integer from, Integer size);

    void deleteUser(Long userId);
}
