package ru.practicum.category.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CategoryDto {

    private Long id;

    @NotBlank(message = "Name can't be blank")
    @Size(min = 1, max = 50)
    private String name;
}