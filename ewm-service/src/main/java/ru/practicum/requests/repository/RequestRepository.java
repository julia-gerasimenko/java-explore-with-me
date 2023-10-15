package ru.practicum.requests.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.events.model.Event;
import ru.practicum.requests.model.Request;
import ru.practicum.util.enam.EventRequestStatus;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface RequestRepository extends JpaRepository<Request, Long> {

    Boolean existsByUserIdAndEventId(Long userId, Long eventId);

    List<Request> findAllByEventIdAndRequestIds(Long eventId, Set<Long> requestIds);

    List<Request> findAllByUser(Long requesterId);

    Optional<Request> findByIdAndUser(Long requestId, Long requesterId);

    List<Request> findAllByEvent(Long eventId);

    List<Request> findAllByEventAndStatus(List<Event> event, EventRequestStatus status);

    Long countRequestByEventAndStatus(Long eventId, EventRequestStatus state);
}