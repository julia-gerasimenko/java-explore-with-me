package ru.practicum.requests.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.format.annotation.DateTimeFormat;
import ru.practicum.util.enam.EventRequestStatus;
import ru.practicum.events.model.Event;
import ru.practicum.users.model.User;

import javax.persistence.*;
import java.time.LocalDateTime;

import static ru.practicum.util.Constants.PATTERN_CREATED_DATE;
import static ru.practicum.util.enam.EventRequestStatus.PENDING;

@Entity
@Table(name = "requests")
@Getter
@Setter
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
@DynamicUpdate
@ToString
public class ParticipationRequest {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false, unique = true)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    @ToString.Exclude
    private Event event;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", updatable = false)
    @ToString.Exclude
    private User requester;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventRequestStatus status = PENDING;

    @Column(name = "created_date", updatable = false, nullable = false)
    @DateTimeFormat(pattern = PATTERN_CREATED_DATE)
    private LocalDateTime created = LocalDateTime.now();
}