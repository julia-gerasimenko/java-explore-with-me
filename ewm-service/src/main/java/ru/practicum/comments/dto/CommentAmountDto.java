package ru.practicum.comments.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CommentAmountDto {
    private Long eventId;
    private Long count;
}
