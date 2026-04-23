package id.ac.ui.cs.advprog.jsonbackend.inventory.event.service;

import id.ac.ui.cs.advprog.jsonbackend.inventory.event.InventoryEventType;
import id.ac.ui.cs.advprog.jsonbackend.inventory.event.OutboxEventStatus;
import id.ac.ui.cs.advprog.jsonbackend.inventory.event.model.InventoryOutboxEvent;
import id.ac.ui.cs.advprog.jsonbackend.inventory.event.repository.InventoryOutboxEventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.doThrow;

@ExtendWith(MockitoExtension.class)
class OutboxEventDispatcherTest {

    @Mock
    private InventoryOutboxEventRepository outboxEventRepository;

    @Mock
    private InventoryEventPublisher eventPublisher;

    @InjectMocks
    private OutboxEventDispatcher dispatcher;

    @Test
    void dispatchPendingEventsShouldMarkEventAsSentWhenPublishSuccess() {
        InventoryOutboxEvent pending = pendingEvent();
        when(outboxEventRepository.findTop50ByStatusOrderByCreatedAtAsc(OutboxEventStatus.PENDING))
                .thenReturn(List.of(pending));

        dispatcher.dispatchPendingEvents();

        verify(eventPublisher, times(1)).publish(pending);
        assertEquals(OutboxEventStatus.SENT, pending.getStatus());
        assertNotNull(pending.getSentAt());
        verify(outboxEventRepository, times(1)).save(pending);
    }

    @Test
    void dispatchPendingEventsShouldMarkEventAsFailedAndIncrementRetryOnPublishFailure() {
        InventoryOutboxEvent pending = pendingEvent();
        when(outboxEventRepository.findTop50ByStatusOrderByCreatedAtAsc(OutboxEventStatus.PENDING))
                .thenReturn(List.of(pending));
        doThrow(new RuntimeException("broker error")).when(eventPublisher).publish(pending);

        dispatcher.dispatchPendingEvents();

        verify(eventPublisher, times(1)).publish(pending);
        assertEquals(OutboxEventStatus.FAILED, pending.getStatus());
        assertEquals(1, pending.getRetryCount());
        verify(outboxEventRepository, times(1)).save(pending);
    }

    private InventoryOutboxEvent pendingEvent() {
        return InventoryOutboxEvent.builder()
                .id(UUID.randomUUID())
                .eventId(UUID.randomUUID())
                .eventType(InventoryEventType.STOCK_RESERVED)
                .aggregateId(UUID.randomUUID())
                .payload("{\"productId\":\"x\",\"quantity\":1}")
                .correlationId("corr-1")
                .status(OutboxEventStatus.PENDING)
                .retryCount(0)
                .occurredAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();
    }
}
