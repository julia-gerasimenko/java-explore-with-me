package ru.practicum.comments.dto;

import lombok.*;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@ToString
public class CommentsAmountDto {
    private Long eventId;
    private Long amount;
}

