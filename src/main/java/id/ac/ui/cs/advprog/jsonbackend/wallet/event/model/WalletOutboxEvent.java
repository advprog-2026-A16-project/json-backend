package id.ac.ui.cs.advprog.jsonbackend.wallet.event.model;

import id.ac.ui.cs.advprog.jsonbackend.wallet.event.WalletEventType;
import id.ac.ui.cs.advprog.jsonbackend.wallet.event.WalletOutboxEventStatus;
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
@Table(name = "wallet_outbox_events")
public class WalletOutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, updatable = false)
    private UUID eventId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WalletEventType eventType;

    @Column(nullable = false, updatable = false)
    private UUID aggregateId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String payload;

    @Column(nullable = false, updatable = false)
    private String correlationId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private WalletOutboxEventStatus status;

    @Column(nullable = false)
    private Integer retryCount;

    @Column(nullable = false, updatable = false)
    private LocalDateTime occurredAt;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private LocalDateTime sentAt;

    @Column(columnDefinition = "TEXT")
    private String failureReason;

    @PrePersist
    public void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        if (eventId == null) {
            eventId = UUID.randomUUID();
        }
        if (status == null) {
            status = WalletOutboxEventStatus.PENDING;
        }
        if (retryCount == null) {
            retryCount = 0;
        }
        if (occurredAt == null) {
            occurredAt = now;
        }
        if (createdAt == null) {
            createdAt = now;
        }
    }
}
