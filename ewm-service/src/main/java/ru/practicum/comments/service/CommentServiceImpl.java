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
import ru.practicum.comments.repository.CommentsRepository;
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
public class CommentServiceImpl implements CommentsService {

    private final CommentsRepository commentsRepository;
    private final EventRepository eventRepository;
    private final UserRepository userRepository;

    @Override
    public CommentDto getCommentByIdPrivate(Long userId, Long commentId) {
        Comment comment = commentsRepository.findCommentByIdAndAuthorId(commentId, userId)
                .orElseThrow(() -> new NotFoundException("Комментарий с i d = " + commentId + " не был найден."));

        log.info("Получен комментарий с id = {} пользователя с i d = {}.", commentId, userId);
        return mapToCommentDto(comment);
    }

    @Transactional
    @Override
    public CommentDto createCommentPrivate(Long userId, Long eventId, NewCommentDto newCommentDto) {
        CommentDto commentDto = CommentMapper.mapToComment(newCommentDto);

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException("Пользователь с id = " + userId + " не обнаружен."));

        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException("Событие с id = " + eventId + " не обнаружено."));

        if (!event.getState().equals(PUBLISHED)) {
            throw new ValidateException("Событие еще не было опубликовано.");
        }

        Comment comment = commentsRepository.save(mapToComment(user, event, commentDto));

        log.info("Создан комментарий с id = {} пользователя с id = {} к событию с id = {}",
                comment.getId(), userId, eventId);

        return mapToCommentDto(comment);
    }

    @Transactional
    @Override
    public CommentDto updateCommentByIdPrivate(Long userId, Long commentId, NewCommentDto newCommentDto) {
        CommentDto commentDto = CommentMapper.mapToComment(newCommentDto);

        Comment comment = commentsRepository.findCommentByIdAndAuthorId(commentId, userId)
                .orElseThrow(() -> new NotFoundException("Комментарий с i d = " + commentId + " не был найден."));

        comment.setText(commentDto.getText());
        comment.setUpdated(LocalDateTime.now());

        log.info("Обновлен комментарий с id = {}, пользователя с id= {}", commentId, userId);

        return mapToCommentDto(commentsRepository.save(comment));
    }



    @Override
    public List<CommentDto> getCommentsByEventIdPrivate(Long userId, Long eventId, Integer from, Integer size) {
        eventRepository.findByIdAndInitiatorId(eventId, userId)
                .orElseThrow(() -> new NotFoundException("Событие с id = " + eventId + " не было найдено."));

        log.info("Получены комментарии пользователя с id = {} к событию с id = {}", userId, eventId);
        return commentsRepository.findAllCommentsByEventId(eventId, new Pagination(from, size, Sort.unsorted()))
                .stream()
                .map(CommentMapper::mapToCommentDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<CommentDto> getCommentsByAuthorIdPrivate(Long userId, Integer from, Integer size) {
        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id = " + userId + " не обнаружен.");
        }

        log.info("Получены все комментарии пользователя с id = {} в период с {}", userId, from);

        return commentsRepository.findAllCommentsByAuthorId(userId, new Pagination(from, size, Sort.unsorted()))
                .stream()
                .map(CommentMapper::mapToCommentDto)
                .collect(Collectors.toList());
    }

    @Transactional
    @Override
    public void deleteCommentByIdPrivate(Long userId, Long commentId) {
        if (!commentsRepository.existsById(commentId)) {
            throw new NotFoundException("Комментарий с id = " + commentId + " не обнаружен.");
        }

        if (!userRepository.existsById(userId)) {
            throw new NotFoundException("Пользователь с id = " + userId + " не обнаружен.");
        }

        commentsRepository.deleteById(commentId);
        log.info("Удален комментарий с id = {} пользователя с id = {}", commentId, userId);
    }

    @Override
    public CommentDto getCommentByIdAdmin(Long commentId) {
        Comment comment = commentsRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий с id = " + commentId + " не обнаружен."));

        log.info("Получен комментарий с id = {}, admin", commentId);
        return mapToCommentDto(comment);
    }

    @Override
    public CommentDto updateCommentAdmin(Long commentId, NewCommentDto newCommentDto) {
        CommentDto commentDto = CommentMapper.mapToComment(newCommentDto);
        Comment comment = commentsRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("Комментарий с id = " + commentId + " не обнаружен."));

        comment.setText(commentDto.getText());
        comment.setUpdated(LocalDateTime.now());

        log.info("Получен комментарий с id = {}, admin", commentId);
        return mapToCommentDto(commentsRepository.save(comment));
    }

    @Override
    public void deleteCommentByIdAdmin(Long commentId) {
        if (!commentsRepository.existsById(commentId)) {
            throw new NotFoundException("Комментарий с id = " + commentId + " не обнаружен.");
        }
        commentsRepository.deleteById(commentId);
        log.info("Комментарий с id = {} успешно удален, admin", commentId);
    }


    @Override
    public List<CommentDto> getCommentsPublic(Long eventId, String text, Integer from, Integer size) {
        if (!eventRepository.existsById(eventId)) {
            throw new NotFoundException("Событие с id = " + eventId + " не обнаружено.");
        }

        log.info("Получен список комментариев, содержащих в себе следующий текст: {}", text);

        return commentsRepository.findAllCommentsByEventIdAndByText(eventId, text, new Pagination(from, size, Sort.unsorted())).stream()
                .map(CommentMapper::mapToCommentDto)
                .collect(Collectors.toList());
    }
}