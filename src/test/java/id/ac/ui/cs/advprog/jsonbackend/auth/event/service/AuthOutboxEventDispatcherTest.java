package id.ac.ui.cs.advprog.jsonbackend.auth.event.service;

import id.ac.ui.cs.advprog.jsonbackend.auth.event.enums.AuthEventType;
import id.ac.ui.cs.advprog.jsonbackend.auth.event.enums.AuthOutboxEventStatus;
import id.ac.ui.cs.advprog.jsonbackend.auth.event.model.AuthOutboxEvent;
import id.ac.ui.cs.advprog.jsonbackend.auth.event.repository.AuthOutboxEventRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthOutboxEventDispatcherTest {

    @Mock
    private AuthOutboxEventRepository outboxEventRepository;

    @Mock
    private AuthEventPublisher eventPublisher;

    @InjectMocks
    private AuthOutboxEventDispatcher dispatcher;

    @Test
    void dispatchPendingEventsShouldMarkEventAsSentWhenPublishSuccess() {
        AuthOutboxEvent pending = createSampleEvent();
        when(outboxEventRepository.findTop50ByStatusOrderByCreatedAtAsc(AuthOutboxEventStatus.PENDING))
                .thenReturn(List.of(pending));

        dispatcher.dispatchPendingEvents();

        verify(eventPublisher, times(1)).publish(pending);
        assertEquals(AuthOutboxEventStatus.SENT, pending.getStatus());
        assertNotNull(pending.getSentAt());
        assertNull(pending.getFailureReason());
        verify(outboxEventRepository, times(1)).save(pending);
    }

    @Test
    void dispatchPendingEventsShouldMarkEventAsFailedAndIncrementRetryOnPublishFailure() {
        AuthOutboxEvent pending = createSampleEvent();
        when(outboxEventRepository.findTop50ByStatusOrderByCreatedAtAsc(AuthOutboxEventStatus.PENDING))
                .thenReturn(List.of(pending));

        doThrow(new RuntimeException("broker error")).when(eventPublisher).publish(pending);

        dispatcher.dispatchPendingEvents();

        verify(eventPublisher, times(1)).publish(pending);
        assertEquals(AuthOutboxEventStatus.FAILED, pending.getStatus());
        assertEquals(1, pending.getRetryCount());
        assertEquals("broker error", pending.getFailureReason());
        verify(outboxEventRepository, times(1)).save(pending);
    }

    @Test
    void dispatchPendingEventsShouldMoveToDeadLetterWhenRetryLimitReached() {
        AuthOutboxEvent pending = createSampleEvent();
        pending.setRetryCount(2);
        when(outboxEventRepository.findTop50ByStatusOrderByCreatedAtAsc(AuthOutboxEventStatus.PENDING))
                .thenReturn(List.of(pending));

        doThrow(new RuntimeException("broker still down")).when(eventPublisher).publish(pending);

        dispatcher.dispatchPendingEvents();

        verify(eventPublisher, times(1)).publish(pending);
        assertEquals(AuthOutboxEventStatus.DEAD_LETTER, pending.getStatus());
        assertEquals(3, pending.getRetryCount());
        verify(outboxEventRepository, times(1)).save(pending);
    }

    @Test
    void requeueFailedEventsShouldMoveFailedToPendingWhenRetryBelowLimit() {
        AuthOutboxEvent failed = createSampleEvent();
        failed.setStatus(AuthOutboxEventStatus.FAILED);
        failed.setRetryCount(1);
        failed.setFailureReason("temporary broker outage");

        when(outboxEventRepository.findTop50ByStatusOrderByOccurredAtAsc(AuthOutboxEventStatus.FAILED))
                .thenReturn(List.of(failed));

        dispatcher.requeueFailedEvents();

        assertEquals(AuthOutboxEventStatus.PENDING, failed.getStatus());
        assertNull(failed.getFailureReason());
        verify(outboxEventRepository, times(1)).save(failed);
    }

    @Test
    void requeueFailedEventsShouldMoveToDeadLetterWhenRetryLimitAlreadyReached() {
        AuthOutboxEvent failed = createSampleEvent();
        failed.setStatus(AuthOutboxEventStatus.FAILED);
        failed.setRetryCount(3);

        when(outboxEventRepository.findTop50ByStatusOrderByOccurredAtAsc(AuthOutboxEventStatus.FAILED))
                .thenReturn(List.of(failed));

        dispatcher.requeueFailedEvents();

        assertEquals(AuthOutboxEventStatus.DEAD_LETTER, failed.getStatus());
        verify(outboxEventRepository, times(1)).save(failed);
    }

    private AuthOutboxEvent createSampleEvent() {
        return AuthOutboxEvent.builder()
                .id(UUID.randomUUID())
                .eventId(UUID.randomUUID())
                .eventType(AuthEventType.USER_REGISTERED.name())
                .aggregateId(UUID.randomUUID())
                .payload("{\"userId\":\"x\",\"email\":\"test@test.com\"}")
                .correlationId("corr-1")
                .status(AuthOutboxEventStatus.PENDING)
                .retryCount(0)
                .occurredAt(LocalDateTime.now())
                .createdAt(LocalDateTime.now())
                .build();
    }
}