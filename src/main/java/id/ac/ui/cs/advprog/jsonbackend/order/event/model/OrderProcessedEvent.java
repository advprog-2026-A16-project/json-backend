package id.ac.ui.cs.advprog.jsonbackend.order.event.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(
        name = "order_processed_events",
        uniqueConstraints = @UniqueConstraint(name = "uk_order_processed_event", columnNames = {"event_id", "handler_name"})
)
public class OrderProcessedEvent {

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