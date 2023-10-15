package ru.practicum.comments.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class NewCommentDto {
    @NotBlank(message = "Комментарий не может быть пустым.")
    @Size(min = 1, max = 10000)
    private String text;
}