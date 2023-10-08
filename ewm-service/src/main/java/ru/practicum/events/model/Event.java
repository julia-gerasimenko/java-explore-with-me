package ru.practicum.events.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.format.annotation.DateTimeFormat;
import ru.practicum.category.model.Category;
import ru.practicum.users.model.User;
import ru.practicum.util.enam.EventState;

import javax.persistence.*;
import java.time.LocalDateTime;

import static ru.practicum.util.Constants.DATE_DEFAULT;
import static ru.practicum.util.enam.EventState.PENDING;

@Getter
@Setter
@Entity
@Table(name = "events")
@ToString
@DynamicUpdate
public class Event {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(updatable = false)
    private Long id;

    @Column(nullable = false, length = 120)
    private String title;

    @Column(nullable = false, length = 2000)
    private String annotation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @Column(nullable = false, length = 7000)
    private String description;

    @Column(name = "event_date", nullable = false)
    @DateTimeFormat(pattern = DATE_DEFAULT)
    private LocalDateTime eventDate;

    @JoinColumn(name = "location_id")
    @ToString.Exclude
    @ManyToOne(fetch = FetchType.LAZY)
    private Location location;

    @Column(nullable = false)
    private Boolean paid;

    @Column(name = "participant_limit", nullable = false)
    private Integer participantLimit;

    @Column(name = "confirmed_requests")
    private Integer confirmedRequests = 0;

    @Column(name = "request_moderation", nullable = false)
    private Boolean requestModeration;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "initiator_id", nullable = false)
    private User initiator;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private EventState state = PENDING;

    @DateTimeFormat(pattern = DATE_DEFAULT)
    @Column(name = "created_on", nullable = false)
    private LocalDateTime createdOn = LocalDateTime.now();

    @DateTimeFormat(pattern = DATE_DEFAULT)
    @Column(name = "published_on")
    private LocalDateTime publishedOn;
}