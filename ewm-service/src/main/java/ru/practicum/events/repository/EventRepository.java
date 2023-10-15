package ru.practicum.events.repository;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import ru.practicum.util.enam.EventStates;
import ru.practicum.events.model.Event;
import ru.practicum.util.Pagination;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface EventRepository extends JpaRepository<Event, Long> {

    List<Event> findAllByIdIn(Set<Long> events);

    Optional<Event> findByIdAndInitiatorId(Long eventId, Long initiatorId);

    @Query("SELECT MIN(e.publishedOn) FROM Event e WHERE e.id IN :eventsId")
    Optional<LocalDateTime> getStart(@Param("eventsId") Collection<Long> eventsId);

    @Query("SELECT e " +
            "FROM Event AS e " +
            "LEFT join fetch e.initiator i " +
            "LEFT join fetch e.category " +
            "where i.id = :userId"
    )
    List<Event> findAllWithInitiatorByInitiatorId(Long userId, Pagination paginationSetup);

    @Query("SELECT e " +
            "FROM Event AS e " +
            "JOIN FETCH e.initiator " +
            "JOIN FETCH e.category " +
            "WHERE e.eventDate > :rangeStart " +
            "AND (e.category.id IN :categories OR :categories IS NULL) " +
            "AND (e.initiator.id IN :users OR :users IS NULL) " +
            "AND (e.state IN :states OR :states IS NULL)"
    )
    List<Event> findAllForAdmin(List<Long> users, List<EventStates> states, List<Long> categories,
                                LocalDateTime rangeStart, PageRequest pageable);

    @Query(
            "SELECT e " +
                    "FROM Event e " +
                    "JOIN FETCH e.initiator i " +
                    "JOIN FETCH e.category c " +
                    "WHERE e.state = :state " +
                    "AND (e.category.id IN :categories OR :categories IS NULL) " +
                    "AND e.eventDate > :rangeStart " +
                    "AND (e.paid = :paid OR :paid IS NULL) " +
                    "AND (:text IS NULL OR " +
                    "(UPPER(e.annotation) LIKE UPPER(CONCAT('%', :text, '%'))) " +
                    "OR (UPPER(e.description) LIKE UPPER(CONCAT('%', :text, '%'))) " +
                    "OR (UPPER(e.title) LIKE UPPER(CONCAT('%', :text, '%'))))"
    )
    List<Event> findAllPublishStateAvailable(EventStates state, LocalDateTime rangeStart, List<Long> categories,
                                             Boolean paid, String text, Pagination pageable);

    @Query("SELECT e " +
            "FROM Event AS e " +
            "JOIN FETCH e.initiator " +
            "JOIN FETCH e.category " +
            "WHERE e.state = :state " +
            "AND (e.category.id IN :categories OR :categories IS NULL) " +
            "AND e.eventDate > :rangeStart " +
            "AND (:paid IS NULL OR e.paid = :paid) " +
            "AND (:text IS NULL OR UPPER(e.annotation) LIKE UPPER(:searchPattern)) " +
            "OR (:text IS NULL OR UPPER(e.description) LIKE UPPER(:searchPattern))"
    )
    List<Event> findAllPublishStateNotAvailable(EventStates state, LocalDateTime rangeStart, List<Long> categories,
                                                Boolean paid, String text, Pagination pageable);


}