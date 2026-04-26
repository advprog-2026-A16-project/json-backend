package id.ac.ui.cs.advprog.jsonbackend.inventory.event.service;

import id.ac.ui.cs.advprog.jsonbackend.inventory.event.InventoryEventType;
import id.ac.ui.cs.advprog.jsonbackend.inventory.event.NonRetryableInventoryEventException;
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
        pending.setFailureReason("old failure");
        when(outboxEventRepository.findTop50ByStatusOrderByCreatedAtAsc(OutboxEventStatus.PENDING))
                .thenReturn(List.of(pending));

        dispatcher.dispatchPendingEvents();

        verify(eventPublisher, times(1)).publish(pending);
        assertEquals(OutboxEventStatus.SENT, pending.getStatus());
        assertNotNull(pending.getSentAt());
        assertEquals(null, pending.getFailureReason());
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
        assertEquals("broker error", pending.getFailureReason());
        verify(outboxEventRepository, times(1)).save(pending);
    }

    @Test
    void dispatchPendingEventsShouldMoveToDeadLetterWhenRetryLimitReached() {
        InventoryOutboxEvent pending = pendingEvent();
        pending.setRetryCount(2);
        when(outboxEventRepository.findTop50ByStatusOrderByCreatedAtAsc(OutboxEventStatus.PENDING))
                .thenReturn(List.of(pending));
        doThrow(new RuntimeException("broker still down")).when(eventPublisher).publish(pending);

        dispatcher.dispatchPendingEvents();

        verify(eventPublisher, times(1)).publish(pending);
        assertEquals(OutboxEventStatus.DEAD_LETTER, pending.getStatus());
        assertEquals(3, pending.getRetryCount());
        verify(outboxEventRepository, times(1)).save(pending);
    }

    @Test
    void dispatchPendingEventsShouldMoveToDeadLetterOnNonRetryableFailure() {
        InventoryOutboxEvent pending = pendingEvent();
        when(outboxEventRepository.findTop50ByStatusOrderByCreatedAtAsc(OutboxEventStatus.PENDING))
                .thenReturn(List.of(pending));
        doThrow(new NonRetryableInventoryEventException("unsupported event type"))
                .when(eventPublisher).publish(pending);

        dispatcher.dispatchPendingEvents();

        verify(eventPublisher, times(1)).publish(pending);
        assertEquals(OutboxEventStatus.DEAD_LETTER, pending.getStatus());
        assertEquals(0, pending.getRetryCount());
        assertEquals("unsupported event type", pending.getFailureReason());
        verify(outboxEventRepository, times(1)).save(pending);
    }

    @Test
    void requeueFailedEventsShouldMoveFailedToPendingWhenRetryBelowLimit() {
        InventoryOutboxEvent failed = pendingEvent();
        failed.setStatus(OutboxEventStatus.FAILED);
        failed.setRetryCount(1);

        when(outboxEventRepository.findTop50ByStatusOrderByOccurredAtAsc(OutboxEventStatus.FAILED))
                .thenReturn(List.of(failed));

        dispatcher.requeueFailedEvents();

        assertEquals(OutboxEventStatus.PENDING, failed.getStatus());
        verify(outboxEventRepository, times(1)).save(failed);
    }

    @Test
    void requeueFailedEventsShouldNotRequeueDeadLetterCandidate() {
        dispatcher.requeueFailedEvents();

        verify(outboxEventRepository, times(1))
                .findTop50ByStatusOrderByOccurredAtAsc(OutboxEventStatus.FAILED);
        verify(outboxEventRepository, times(0)).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void requeueFailedEventsShouldMoveToDeadLetterWhenRetryLimitAlreadyReached() {
        InventoryOutboxEvent failed = pendingEvent();
        failed.setStatus(OutboxEventStatus.FAILED);
        failed.setRetryCount(3);

        when(outboxEventRepository.findTop50ByStatusOrderByOccurredAtAsc(OutboxEventStatus.FAILED))
                .thenReturn(List.of(failed));

        dispatcher.requeueFailedEvents();

        assertEquals(OutboxEventStatus.DEAD_LETTER, failed.getStatus());
        verify(outboxEventRepository, times(1)).save(failed);
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
