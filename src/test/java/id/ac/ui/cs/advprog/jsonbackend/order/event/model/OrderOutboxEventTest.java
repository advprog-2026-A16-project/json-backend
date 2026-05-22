package id.ac.ui.cs.advprog.jsonbackend.order.event.model;

import id.ac.ui.cs.advprog.jsonbackend.order.event.OrderEventType;
import id.ac.ui.cs.advprog.jsonbackend.order.event.OutboxEventStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OrderOutboxEventTest {

    @Test
    void onCreateInitializesDefaults() {
        OrderOutboxEvent event = OrderOutboxEvent.builder()
                .eventType(OrderEventType.ORDER_CREATED)
                .aggregateId(UUID.randomUUID())
                .payload("{\"ok\":true}")
                .correlationId("corr-1")
                .build();

        event.onCreate();

        assertNotNull(event.getEventId());
        assertEquals(OutboxEventStatus.PENDING, event.getStatus());
        assertEquals(0, event.getRetryCount());
        assertNotNull(event.getOccurredAt());
        assertNotNull(event.getCreatedAt());
    }

    @Test
    void onCreatePreservesExplicitValues() {
        UUID eventId = UUID.randomUUID();
        LocalDateTime occurredAt = LocalDateTime.of(2026, 5, 21, 10, 0);
        OrderOutboxEvent event = OrderOutboxEvent.builder()
                .eventId(eventId)
                .eventType(OrderEventType.ORDER_CANCELLED)
                .aggregateId(UUID.randomUUID())
                .payload("{\"ok\":false}")
                .correlationId("corr-2")
                .status(OutboxEventStatus.FAILED)
                .retryCount(3)
                .occurredAt(occurredAt)
                .build();

        event.onCreate();

        assertEquals(eventId, event.getEventId());
        assertEquals(OutboxEventStatus.FAILED, event.getStatus());
        assertEquals(3, event.getRetryCount());
        assertEquals(occurredAt, event.getOccurredAt());
        assertNotNull(event.getCreatedAt());
    }
}
