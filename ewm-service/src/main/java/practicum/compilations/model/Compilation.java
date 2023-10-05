package practicum.compilations.model;

import lombok.*;
import practicum.events.model.Event;

import javax.persistence.*;
import java.util.Objects;
import java.util.Set;

@Entity
@Table(name = "compilations")
@Getter
@Setter
@Builder
@ToString
@NoArgsConstructor
@AllArgsConstructor
public class Compilation {

    @Id
    @Column(updatable = false)
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "title", nullable = false)
    private String title;

    @Column(nullable = false)
    private Boolean pinned;

    @ManyToMany
    @ToString.Exclude
    @JoinTable(name = "compilation_events", joinColumns = @JoinColumn(name = "compilation_id"),
            inverseJoinColumns = @JoinColumn(name = "event_id"))
    private Set<Event> events;

    @Override
    public final boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Compilation that = (Compilation) o;
        return getId() != null && Objects.equals(getId(), that.getId());
    }

    @Override
    public final int hashCode() {
        return Objects.hash(getId());
    }
}