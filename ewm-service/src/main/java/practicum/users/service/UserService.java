package practicum.users.service;

import practicum.users.dto.NewUserRequest;
import practicum.users.dto.UserDto;

import java.util.List;

public interface UserService {
    UserDto createUser(NewUserRequest newUserRequest);

    List<UserDto> getUsers(List<Long> ids, Integer from, Integer size);

    void deleteUserById(Long userId);
}