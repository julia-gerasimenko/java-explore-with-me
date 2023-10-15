package ru.practicum.comments.dto;

import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class NewCommentDto {

    @Size(min = 2, max = 5000)
    @NotBlank(message = "Комментарий не может быть пустым")
    private String text;
}