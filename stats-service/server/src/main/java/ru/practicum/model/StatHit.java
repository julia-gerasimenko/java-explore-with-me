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
    @Column(nullable = false)
    private int id;

    @Column(nullable = false)
    private String app;

    @Column(nullable = false)
    private String uri;

    @Column(nullable = false)
    private String ip;


    @Column(name = "created", nullable = false)
    private LocalDateTime timestamp;
}