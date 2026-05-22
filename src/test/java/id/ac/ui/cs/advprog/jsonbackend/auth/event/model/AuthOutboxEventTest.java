package id.ac.ui.cs.advprog.jsonbackend.auth.event.model;

import id.ac.ui.cs.advprog.jsonbackend.auth.event.enums.AuthOutboxEventStatus;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AuthOutboxEventTest {

    @Test
    void onCreateInitializesDefaults() {
        AuthOutboxEvent event = AuthOutboxEvent.builder()
                .eventType("USER_REGISTERED")
                .aggregateId(UUID.randomUUID())
                .payload("{\"user\":\"a\"}")
                .correlationId("corr-auth")
                .build();

        event.onCreate();

        assertNotNull(event.getEventId());
        assertEquals(AuthOutboxEventStatus.PENDING, event.getStatus());
        assertEquals(0, event.getRetryCount());
        assertNotNull(event.getOccurredAt());
        assertNotNull(event.getCreatedAt());
    }

    @Test
    void onCreatePreservesExplicitValues() {
        UUID eventId = UUID.randomUUID();
        LocalDateTime occurredAt = LocalDateTime.of(2026, 5, 21, 11, 0);
        AuthOutboxEvent event = AuthOutboxEvent.builder()
                .eventId(eventId)
                .eventType("USER_REGISTERED")
                .aggregateId(UUID.randomUUID())
                .payload("{\"user\":\"b\"}")
                .correlationId("corr-auth-2")
                .status(AuthOutboxEventStatus.DEAD_LETTER)
                .retryCount(4)
                .occurredAt(occurredAt)
                .build();

        event.onCreate();

        assertEquals(eventId, event.getEventId());
        assertEquals(AuthOutboxEventStatus.DEAD_LETTER, event.getStatus());
        assertEquals(4, event.getRetryCount());
        assertEquals(occurredAt, event.getOccurredAt());
        assertNotNull(event.getCreatedAt());
    }
}
