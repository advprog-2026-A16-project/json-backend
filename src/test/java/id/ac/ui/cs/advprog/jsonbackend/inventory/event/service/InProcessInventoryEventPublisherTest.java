package id.ac.ui.cs.advprog.jsonbackend.inventory.event.service;

import id.ac.ui.cs.advprog.jsonbackend.inventory.event.InventoryEventType;
import id.ac.ui.cs.advprog.jsonbackend.inventory.event.NonRetryableInventoryEventException;
import id.ac.ui.cs.advprog.jsonbackend.inventory.event.model.InventoryOutboxEvent;
import id.ac.ui.cs.advprog.jsonbackend.inventory.event.handler.StockReleaseRequestedEventHandler;
import id.ac.ui.cs.advprog.jsonbackend.inventory.event.handler.StockReservationRequestedEventHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class InProcessInventoryEventPublisherTest {

    @Mock
    private StockReservationRequestedEventHandler stockReservationRequestedEventHandler;

    @Mock
    private StockReleaseRequestedEventHandler stockReleaseRequestedEventHandler;

    @InjectMocks
    private InProcessInventoryEventPublisher publisher;

    @Test
    void publishShouldNoopForStockReservedEvent() {
        UUID productId = UUID.randomUUID();
        InventoryOutboxEvent event = InventoryOutboxEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType(InventoryEventType.STOCK_RESERVED)
                .aggregateId(productId)
                .payload("{\"productId\":\"" + productId + "\",\"quantity\":0}")
                .correlationId("corr-zero-qty")
                .build();

        publisher.publish(event);
        verifyNoInteractions(stockReservationRequestedEventHandler, stockReleaseRequestedEventHandler);
    }

    @Test
    void publishShouldRejectBlankCorrelationIdAsNonRetryable() {
        UUID productId = UUID.randomUUID();
        InventoryOutboxEvent event = InventoryOutboxEvent.builder()
                .eventId(UUID.randomUUID())
                .eventType(InventoryEventType.STOCK_RESERVED)
                .aggregateId(productId)
                .payload("{\"productId\":\"" + productId + "\",\"quantity\":1}")
                .correlationId("   ")
                .build();

        assertThrows(NonRetryableInventoryEventException.class, () -> publisher.publish(event));
        verifyNoInteractions(stockReservationRequestedEventHandler, stockReleaseRequestedEventHandler);
    }
}
