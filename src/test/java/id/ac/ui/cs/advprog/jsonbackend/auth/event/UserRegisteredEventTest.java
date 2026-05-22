package id.ac.ui.cs.advprog.jsonbackend.auth.event;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class UserRegisteredEventTest {

    @Test
    void recordExposesAllFields() {
        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UUID profileId = UUID.randomUUID();

        UserRegisteredEvent event = new UserRegisteredEvent(
                eventId,
                userId,
                "user@example.com",
                "JASTIPER",
                "ACTIVE",
                profileId,
                "rifqy",
                4.5,
                7,
                "corr-user"
        );

        assertEquals(eventId, event.eventId());
        assertEquals(userId, event.userId());
        assertEquals("user@example.com", event.email());
        assertEquals("JASTIPER", event.role());
        assertEquals("ACTIVE", event.accountStatus());
        assertEquals(profileId, event.profileId());
        assertEquals("rifqy", event.username());
        assertEquals(4.5, event.rating());
        assertEquals(7, event.successfulTransactions());
        assertEquals("corr-user", event.correlationId());
    }
}
