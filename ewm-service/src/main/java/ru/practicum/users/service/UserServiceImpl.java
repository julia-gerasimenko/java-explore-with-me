package ru.practicum.users.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.handler.NotFoundException;
import ru.practicum.users.dto.NewUserRequest;
import ru.practicum.users.dto.UserDto;
import ru.practicum.users.dto.UserMapper;
import ru.practicum.users.model.User;
import ru.practicum.users.repository.UserRepository;
import ru.practicum.util.Pagination;

import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.users.dto.UserMapper.toUser;
import static ru.practicum.users.dto.UserMapper.toUserDto;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    public UserDto createUser(NewUserRequest userDto) {
        User user = userRepository.save(toUser(userDto));
        log.info("Create user {}", user);
        return toUserDto(user);
    }

    @Transactional(readOnly = true)
    @Override
    public List<UserDto> getUsers(List<Long> ids, Integer from, Integer size) {
        log.info("Get user with ids: {}", ids);
        if (ids.isEmpty()) {
            return userRepository.findAll(new Pagination(from, size, Sort.unsorted())).stream()
                    .map(UserMapper::toUserDto)
                    .collect(Collectors.toList());
        } else {
            return userRepository.findAllByIdIn(ids, new Pagination(from, size, Sort.unsorted())).stream()
                    .map(UserMapper::toUserDto)
                    .collect(Collectors.toList());
        }
    }

    @Override
    public void deleteUserById(Long userId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id=" + userId + " hasn't found");
        }
        log.info("Delete user with id= {}", userId);
        userRepository.deleteById(userId);
    }
}