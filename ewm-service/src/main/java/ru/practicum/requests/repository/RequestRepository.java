package ru.practicum.requests.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.events.model.Event;
import ru.practicum.requests.model.Request;
import ru.practicum.util.enam.EventRequestStatus;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public interface RequestRepository extends JpaRepository<Request, Long> {

    Boolean existsByRequesterIdAndEventId(Long userId, Long eventId);

    List<Request> findAllByEventIdAndIdIn(Long eventId, Set<Long> requestIds);

    List<Request> findAllByRequesterId(Long requesterId);

    Optional<Request> findByIdAndRequesterId(Long requestId, Long requesterId);

    List<Request> findAllByEventId(Long eventId);

    List<Request> findAllByEventInAndStatus(List<Event> event, EventRequestStatus status);

    Long countRequestByEventIdAndStatus(Long eventId, EventRequestStatus status);
}