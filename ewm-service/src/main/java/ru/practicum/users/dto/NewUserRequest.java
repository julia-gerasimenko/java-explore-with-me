package ru.practicum.users.dto;

import lombok.Getter;
import lombok.ToString;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@ToString
public class NewUserRequest {

    @Email
    @NotBlank(message = "Email не может быть пустым")
    @Size(min = 10, max = 50)
    private String email;

    @NotBlank(message = "Имя не может быть пустым")
    @Size(min = 2, max = 50)
    private String name;
}