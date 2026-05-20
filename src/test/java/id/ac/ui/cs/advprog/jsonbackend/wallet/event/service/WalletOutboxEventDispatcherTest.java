package id.ac.ui.cs.advprog.jsonbackend.wallet.event.service;

import id.ac.ui.cs.advprog.jsonbackend.wallet.event.WalletEventType;
import id.ac.ui.cs.advprog.jsonbackend.wallet.event.WalletOutboxEventStatus;
import id.ac.ui.cs.advprog.jsonbackend.wallet.event.model.WalletOutboxEvent;
import id.ac.ui.cs.advprog.jsonbackend.wallet.event.repository.WalletOutboxEventRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

class WalletOutboxEventDispatcherTest {

    private WalletOutboxEventRepository repository;
    private WalletEventPublisher eventPublisher;
    private WalletOutboxEventDispatcher dispatcher;

    @BeforeEach
    void setUp() {
        repository = mock(WalletOutboxEventRepository.class);
        eventPublisher = mock(WalletEventPublisher.class);
        dispatcher = new WalletOutboxEventDispatcher(repository, eventPublisher);
    }

    @Test
    void dispatchPendingEventsShouldMarkEventAsSentWhenPublishSucceeds() {
        WalletOutboxEvent event = walletOutboxEvent();
        when(repository.findTop50ByStatusOrderByCreatedAtAsc(WalletOutboxEventStatus.PENDING))
                .thenReturn(List.of(event));

        dispatcher.dispatchPendingEvents();

        verify(eventPublisher).publish(event);
        assertEquals(WalletOutboxEventStatus.SENT, event.getStatus());
        assertNotNull(event.getSentAt());
        verify(repository).save(event);
    }

    @Test
    void dispatchPendingEventsShouldMarkEventAsFailedWhenPublishFailsBelowRetryLimit() {
        WalletOutboxEvent event = walletOutboxEvent();
        when(repository.findTop50ByStatusOrderByCreatedAtAsc(WalletOutboxEventStatus.PENDING))
                .thenReturn(List.of(event));
        doThrow(new RuntimeException("downstream unavailable")).when(eventPublisher).publish(event);

        dispatcher.dispatchPendingEvents();

        assertEquals(WalletOutboxEventStatus.FAILED, event.getStatus());
        assertEquals(1, event.getRetryCount());
        assertEquals("downstream unavailable", event.getFailureReason());
    }

    @Test
    void requeueFailedEventsShouldMoveFailedEventBackToPendingWhenRetryBelowLimit() {
        WalletOutboxEvent event = walletOutboxEvent();
        event.setStatus(WalletOutboxEventStatus.FAILED);
        event.setRetryCount(1);
        event.setFailureReason("temporary");
        when(repository.findTop50ByStatusOrderByOccurredAtAsc(WalletOutboxEventStatus.FAILED))
                .thenReturn(List.of(event));

        dispatcher.requeueFailedEvents();

        assertEquals(WalletOutboxEventStatus.PENDING, event.getStatus());
        assertEquals(null, event.getFailureReason());
        verify(repository).save(event);
    }

    private WalletOutboxEvent walletOutboxEvent() {
        WalletOutboxEvent event = WalletOutboxEvent.builder()
                .eventType(WalletEventType.PAYMENT_SUCCESS)
                .aggregateId(UUID.randomUUID())
                .payload("{}")
                .correlationId("corr")
                .build();
        event.onCreate();
        return event;
    }
}
