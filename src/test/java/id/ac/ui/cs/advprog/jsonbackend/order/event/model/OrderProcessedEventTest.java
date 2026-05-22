package id.ac.ui.cs.advprog.jsonbackend.order.event.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class OrderProcessedEventTest {

    @Test
    void onCreateSetsProcessedAtTimestamp() {
        OrderProcessedEvent event = OrderProcessedEvent.builder()
                .eventId(UUID.randomUUID())
                .handlerName("wallet-handler")
                .build();

        event.onCreate();

        assertNotNull(event.getProcessedAt());
        assertEquals("wallet-handler", event.getHandlerName());
    }
}
