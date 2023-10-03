package ru.practicum.model;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "stat")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StatHit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    private String app;

    private String uri;

    private String ip;

    @Column(name = "created")
    private LocalDateTime timestamp;
}