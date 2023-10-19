package ru.practicum.comments.dto;

import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@ToString
public class CommentCountDto {

    private Long eventId;
    private Long count;

}
