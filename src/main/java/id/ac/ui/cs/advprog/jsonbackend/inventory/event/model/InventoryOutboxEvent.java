package id.ac.ui.cs.advprog.jsonbackend.inventory.event.model;

import id.ac.ui.cs.advprog.jsonbackend.inventory.event.InventoryEventType;
import id.ac.ui.cs.advprog.jsonbackend.inventory.event.OutboxEventStatus;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
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
@Table(name = "inventory_outbox_events")
public class InventoryOutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID eventId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private InventoryEventType eventType;

    @Column(nullable = false, updatable = false)
    private UUID aggregateId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(nullable = false, updatable = false)
    private String correlationId;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private OutboxEventStatus status;

    @Column(nullable = false)
    private Integer retryCount;

    @Column(nullable = false, updatable = false)
    private LocalDateTime occurredAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime sentAt;

    @PrePersist
    void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (this.eventId == null) {
            this.eventId = UUID.randomUUID();
        }
        if (this.status == null) {
            this.status = OutboxEventStatus.PENDING;
        }
        if (this.retryCount == null) {
            this.retryCount = 0;
        }
        if (this.occurredAt == null) {
            this.occurredAt = now;
        }
        this.createdAt = now;
    }
}
