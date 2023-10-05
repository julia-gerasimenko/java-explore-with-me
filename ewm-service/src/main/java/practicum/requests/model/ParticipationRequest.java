package practicum.requests.model;

import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.format.annotation.DateTimeFormat;
import practicum.events.model.Event;
import practicum.users.model.User;
import practicum.util.enam.EventRequestStatus;

import javax.persistence.*;
import java.time.LocalDateTime;

import static practicum.util.Constants.PATTERN_CREATED_DATE;
import static practicum.util.enam.EventRequestStatus.PENDING;

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

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id")
    private Event event;

    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "requester_id", updatable = false)
    private User requester;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EventRequestStatus status = PENDING;

    @DateTimeFormat(pattern = PATTERN_CREATED_DATE)
    @Column(name = "created_date", updatable = false, nullable = false)
    private LocalDateTime created = LocalDateTime.now();
}