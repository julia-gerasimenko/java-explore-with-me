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
    @Size(min = 6, max = 254)
    @NotBlank(message = "Email не может быть пустым")
    private String email;

    @Size(min = 2, max = 250)
    @NotBlank(message = "Имя не может быть пустым")
    private String name;
}