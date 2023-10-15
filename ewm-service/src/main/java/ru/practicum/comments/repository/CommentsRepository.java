package ru.practicum.comments.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.comments.dto.CommentsAmountDto;
import ru.practicum.comments.model.Comment;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface CommentsRepository extends JpaRepository<Comment, Long> {

    Long countCommentsByEventId(Long eventId);

    @Query("SELECT new ru.practicum.comments.dto.CommentCountDto(c.event.id, COUNT(c.event.id)) " +
            "FROM Comment c WHERE c.event.id IN :eventIdList GROUP BY c.event.id")
    List<CommentsAmountDto> findCommentCountByEventIdList(@Param("eventIdList") Set<Long> eventIdList);

    Optional<Comment> findCommentByIdAndAuthorId(Long commentId, Long userId);

    List<Comment> findAllCommentsByEventId(Long eventId, PageRequest pageable);

    Page<Comment> findAllCommentsByAuthorId(Long userId, PageRequest pageable);

    @Query("SELECT c " +
            "FROM Comment c " +
            "JOIN FETCH c.event " +
            "JOIN FETCH c.author " +
            "where (c.event.id = :event) " +
            "and (UPPER(c.text) LIKE UPPER(concat('%', :text, '%')))"
    )
    List<Comment> findAllCommentsByEventIdAndByText(@Param("event") Long eventId, @Param("text") String text, PageRequest pageable);
}