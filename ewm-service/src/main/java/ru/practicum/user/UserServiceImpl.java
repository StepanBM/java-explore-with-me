package ru.practicum.user;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.practicum.exceptions.ConflictErrorException;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;

import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.user.UserMapper.mapToUser;
import static ru.practicum.user.UserMapper.mapToUserDto;

@Service
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Autowired
    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public List<UserDto> findAllUsers(List<Long> ids, Integer from, Integer size) {
        log.info("Начало поиска пользователей: ids={}, from={}, size={}", ids, from, size);
        Pageable pageable = PageRequest.of(from / size, size);

        Page<User> usersPage;

        if (ids != null && !ids.isEmpty()) {
            log.debug("Поиск пользователей по списку id: количество ids={}", ids.size());
            usersPage = userRepository.findAllByIdIn(ids, pageable);
        } else {
            log.debug("Поиск всех пользователей");
            usersPage = userRepository.findAll(pageable);
        }

        return usersPage.getContent().stream()
                .map(UserMapper::mapToUserDto)
                .collect(Collectors.toList());
    }


    @Override
    @Transactional
    public UserDto addUser(NewUserRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ConflictErrorException("Email already exists: '%s'".formatted(request.getEmail()));
        }
        log.debug("Начинается добавление пользователя по запросу {}", request);
        User user = mapToUser(request);
        log.debug("Запрос на добавление пользователя конвертирован в объект класса User {}", user);
        user = userRepository.save(user);
        log.debug("Добавлен пользователь {}", user);
        return mapToUserDto(user);
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
        log.debug("Пользователь {} успешно удалён.", id);
    }

}
