package practicum.users.controllers;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import practicum.users.dto.NewUserRequest;
import practicum.users.dto.UserDto;
import practicum.users.service.UserService;

import javax.validation.Valid;
import javax.validation.constraints.Positive;
import javax.validation.constraints.PositiveOrZero;
import java.util.List;

import static practicum.util.Constants.PAGE_DEFAULT_FROM;
import static practicum.util.Constants.PAGE_DEFAULT_SIZE;

@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
@Validated
@Slf4j
public class UserAdminController {

    private final UserService userService;

    @PostMapping
    @ResponseStatus(value = HttpStatus.CREATED)
    public UserDto create(@Valid @RequestBody NewUserRequest newUserRequest) {
        return userService.createUser(newUserRequest);
    }

    @DeleteMapping("/{userId}")
    @ResponseStatus(value = HttpStatus.NO_CONTENT)
    public void deleteUser(@PathVariable(value = "userId") Long userId) {
        userService.deleteUserById(userId);
    }

    @GetMapping()
    public List<UserDto> get(@RequestParam(defaultValue = "") List<Long> ids,
                             @RequestParam(value = "from"
                                     , defaultValue = PAGE_DEFAULT_FROM) @PositiveOrZero Integer from,
                             @RequestParam(value = "size", defaultValue = PAGE_DEFAULT_SIZE) @Positive Integer size) {
        return userService.getUsers(ids, from, size);
    }
}