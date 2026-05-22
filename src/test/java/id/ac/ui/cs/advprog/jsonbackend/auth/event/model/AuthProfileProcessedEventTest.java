package id.ac.ui.cs.advprog.jsonbackend.auth.event.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class AuthProfileProcessedEventTest {

    @Test
    void onCreateSetsProcessedAtTimestamp() {
        AuthProfileProcessedEvent event = AuthProfileProcessedEvent.builder()
                .eventId(UUID.randomUUID())
                .handlerName("profile-handler")
                .build();

        event.onCreate();

        assertNotNull(event.getProcessedAt());
        assertEquals("profile-handler", event.getHandlerName());
    }
}
