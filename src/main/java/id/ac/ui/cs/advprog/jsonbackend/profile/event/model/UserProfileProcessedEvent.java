package id.ac.ui.cs.advprog.jsonbackend.profile.event.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "user_profile_processed_events",
        uniqueConstraints = @UniqueConstraint(name = "uk_profile_processed_event", columnNames = {"event_id", "handler_name"})
)
public class UserProfileProcessedEvent {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "event_id", nullable = false, updatable = false)
    private UUID eventId;

    @Column(name = "handler_name", nullable = false, updatable = false)
    private String handlerName;

    @Column(nullable = false, updatable = false)
    private LocalDateTime processedAt;

    @PrePersist
    void onCreate() {
        this.processedAt = LocalDateTime.now();
    }
}