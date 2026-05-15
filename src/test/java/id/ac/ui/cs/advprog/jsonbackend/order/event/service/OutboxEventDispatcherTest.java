package id.ac.ui.cs.advprog.jsonbackend.order.event.service;

import id.ac.ui.cs.advprog.jsonbackend.order.event.OutboxEventStatus;
import id.ac.ui.cs.advprog.jsonbackend.order.event.model.OrderOutboxEvent;
import id.ac.ui.cs.advprog.jsonbackend.order.event.repository.OrderOutboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OutboxEventDispatcherTest {

    @Mock
    private OrderOutboxEventRepository outboxEventRepository;

    @Mock
    private OrderEventPublisher orderEventPublisher;

    @InjectMocks
    private OutboxEventDispatcher dispatcher;

    private OrderOutboxEvent pendingEvent1;
    private OrderOutboxEvent pendingEvent2;

    @BeforeEach
    void setUp() {
        pendingEvent1 = new OrderOutboxEvent();
        pendingEvent1.setId(UUID.randomUUID());
        pendingEvent1.setStatus(OutboxEventStatus.PENDING);
        pendingEvent1.setRetryCount(0);

        pendingEvent2 = new OrderOutboxEvent();
        pendingEvent2.setId(UUID.randomUUID());
        pendingEvent2.setStatus(OutboxEventStatus.PENDING);
        pendingEvent2.setRetryCount(0);
    }

    @Test
    void testDispatchPendingEventsSuccess() {
        when(outboxEventRepository.findByStatusOrderByCreatedAtAsc(OutboxEventStatus.PENDING))
                .thenReturn(Arrays.asList(pendingEvent1, pendingEvent2));

        dispatcher.dispatchPendingEvents();

        verify(orderEventPublisher).publish(pendingEvent1);
        verify(orderEventPublisher).publish(pendingEvent2);

        assertEquals(OutboxEventStatus.PROCESSED, pendingEvent1.getStatus());
        assertNotNull(pendingEvent1.getSentAt());
        assertEquals(OutboxEventStatus.PROCESSED, pendingEvent2.getStatus());
        assertNotNull(pendingEvent2.getSentAt());

        verify(outboxEventRepository).save(pendingEvent1);
        verify(outboxEventRepository).save(pendingEvent2);
    }

    @Test
    void testDispatchPendingEventsHandlesException() {
        when(outboxEventRepository.findByStatusOrderByCreatedAtAsc(OutboxEventStatus.PENDING))
                .thenReturn(List.of(pendingEvent1));

        doThrow(new RuntimeException("Broker failure")).when(orderEventPublisher).publish(pendingEvent1);

        dispatcher.dispatchPendingEvents();

        assertEquals(OutboxEventStatus.FAILED, pendingEvent1.getStatus());
        assertEquals(1, pendingEvent1.getRetryCount());
        assertNotNull(pendingEvent1.getFailureReason());

        verify(outboxEventRepository).save(pendingEvent1);
    }
}