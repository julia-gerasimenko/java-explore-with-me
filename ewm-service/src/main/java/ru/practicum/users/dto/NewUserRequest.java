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
    @NotBlank(message = "Email can't be blank")
    @Size(min = 6, max = 254)
    private String email;

    @NotBlank(message = "Name can't be blank")
    @Size(min = 2, max = 250)
    private String name;
}