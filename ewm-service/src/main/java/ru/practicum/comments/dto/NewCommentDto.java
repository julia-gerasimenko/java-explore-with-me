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

    @Size(min = 2, max = 2000)
    @NotBlank(message = "Comment can't be blank")
    private String text;
}