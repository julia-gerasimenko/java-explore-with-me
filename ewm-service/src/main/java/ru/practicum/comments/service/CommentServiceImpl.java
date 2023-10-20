package ru.practicum.comments.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.comments.dto.CommentDto;
import ru.practicum.comments.dto.CommentMapper;
import ru.practicum.comments.dto.NewCommentDto;
import ru.practicum.comments.model.Comment;
import ru.practicum.comments.repository.CommentRepository;
import ru.practicum.events.model.Event;
import ru.practicum.events.repository.EventRepository;
import ru.practicum.handler.NotFoundException;
import ru.practicum.handler.ValidateException;
import ru.practicum.users.model.User;
import ru.practicum.users.repository.UserRepository;
import ru.practicum.util.Pagination;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import static ru.practicum.comments.dto.CommentMapper.mapToComment;
import static ru.practicum.comments.dto.CommentMapper.mapToCommentDto;
import static ru.practicum.util.enam.EventState.PUBLISHED;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional(readOnly = true)
public class CommentServiceImpl implements CommentService {

    private final CommentRepository commentRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Transactional
    @Override
    public CommentDto createCommentPrivate(Long userId, Long eventId, NewCommentDto newCommentDto) {
        CommentDto commentDto = CommentMapper.mapToComment(newCommentDto);
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("User with id = " + userId + " does not exist"));
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Event with id = " + eventId + " does not exist"));

        if (!event.getState().equals(PUBLISHED)) {
            throw new ValidateException("Event with id" + eventId + "wasn't published yet.");
        }
        Comment comment = commentRepository.save(mapToComment(user, event, commentDto));

        log.info("Created comment {} of user with id = {} for event with id = {}", comment, userId, eventId);
        return mapToCommentDto(comment);
    }

    @Transactional
    @Override
    public CommentDto updateCommentByIdPrivate(Long userId, Long commentId, NewCommentDto newCommentDto) {
        CommentDto commentDto = CommentMapper.mapToComment(newCommentDto);
        Comment comment = commentRepository.findByIdAndAuthorId(commentId, userId)
                .orElseThrow(() -> new NotFoundException("Comment with id = " + commentId + " does not exist"));

        comment.setText(commentDto.getText());
        comment.setUpdated(LocalDateTime.now());

        log.info("Updated comment {} of user with id= {}", comment, userId);
        return mapToCommentDto(commentRepository.save(comment));
    }

    @Override
    public CommentDto getCommentByIdPrivate(Long userId, Long commentId) {
        Comment comment = commentRepository.findByIdAndAuthorId(commentId, userId)
                .orElseThrow(() -> new NotFoundException("Comment with id = " + commentId + " does not exist"));

        log.info("Got comment with id {} by user with id={}", commentId, commentId);
        return mapToCommentDto(comment);
    }

    @Override
    public List<CommentDto> getCommentsByEventIdPrivate(Long userId, Long eventId, Integer from, Integer size) {
        eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Event with id = " + eventId + " does not exist"));

        log.info("Got all comments by user with id={} and event with id = {} from {}, size {}", userId, eventId, from, size);
        return commentRepository.findAllByEventId(eventId, new Pagination(from, size, Sort.unsorted()))
                .stream()
                .map(CommentMapper::mapToCommentDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommentDto> getCommentsByAuthorIdPrivate(Long userId, Integer from, Integer size) {

        log.info("Got all comments by user with id = {}, from {}, size {}", userId, from, size);
        return commentRepository.findAllByAuthorId(userId, new Pagination(from, size, Sort.unsorted()))
                .stream()
                .map(CommentMapper::mapToCommentDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public void deleteCommentByIdPrivate(Long userId, Long commentId) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("User with id = " + userId + " does not exist");
        }
        if (!commentRepository.existsById(commentId)) {
            throw new NotFoundException("Comment with id = " + commentId + " does not exist");
        }
        log.info("Deleted comment with id = {} of user with id = {}", commentId, userId);
        commentRepository.deleteById(commentId);

    }

    @Override
    public CommentDto getCommentByIdAdmin(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment with id = " + commentId + " does not exist"));
        log.info("Got comment with id = {} on admin part", commentId);
        return mapToCommentDto(comment);
    }

    @Override
    public CommentDto updateCommentAdmin(Long commentId, NewCommentDto newCommentDto) {
        CommentDto commentDto = CommentMapper.mapToComment(newCommentDto);
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Comment with id = " + commentId + " does not exist"));
        comment.setText(commentDto.getText());
        comment.setUpdated(LocalDateTime.now());

        log.info("Updated comment {} with  id={} on admin part", commentDto, commentId);
        return mapToCommentDto(commentRepository.save(comment));
    }

    @Override
    public List<CommentDto> getCommentsPublic(Long eventId, String text, Integer from, Integer size) {
        log.info("Got comment for event with id = {} with text = {} from {}, size {}", eventId, text, from, size);

        return commentRepository.findAllEventIdAndByText(eventId, text, new Pagination(from, size, Sort.unsorted())).stream()
                .map(CommentMapper::mapToCommentDto)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteCommentByIdAdmin(Long commentId) {
        if (!commentRepository.existsById(commentId)) {
            throw new NotFoundException("Comment with id = " + commentId + " does not exist");
        }
        commentRepository.deleteById(commentId);
        log.info("Deleted comment with id = {} on admin part", commentId);
    }
}