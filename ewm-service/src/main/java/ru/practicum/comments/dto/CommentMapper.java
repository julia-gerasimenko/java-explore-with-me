package ru.practicum.comments.dto;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.comments.model.Comment;
import ru.practicum.events.model.Event;
import ru.practicum.users.model.User;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class CommentMapper {

    public static CommentDto mapToComment(NewCommentDto newCommentDto) {
        return CommentDto.builder()
                .text(newCommentDto.getText())
                .build();
    }

    public static CommentDto mapToCommentDto(Comment comment) {
        return CommentDto.builder()
                .id(comment.getId())
                .text(comment.getText())
                .authorId(comment.getAuthor().getId())
                .eventId(comment.getEvent().getId())
                .created(comment.getCreated())
                .build();
    }

    public static Comment mapToComment(User user, Event event, CommentDto dto) {
        Comment comment = new Comment();
        comment.setText(dto.getText());
        comment.setEvent(event);
        comment.setAuthor(user);
        return comment;
    }
}