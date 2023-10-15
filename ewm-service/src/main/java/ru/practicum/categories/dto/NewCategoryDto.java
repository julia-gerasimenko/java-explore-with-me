package ru.practicum.categories.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class NewCategoryDto {

    @NotBlank(message = "Наименование категории не может быть пустым")
    @Size(min = 1, max = 50)
    private String name;
}
