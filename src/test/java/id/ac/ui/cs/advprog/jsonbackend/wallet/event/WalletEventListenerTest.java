package id.ac.ui.cs.advprog.jsonbackend.wallet.event;

import id.ac.ui.cs.advprog.jsonbackend.wallet.dto.PaymentRequest;
import id.ac.ui.cs.advprog.jsonbackend.wallet.exception.InsufficientBalanceException;
import id.ac.ui.cs.advprog.jsonbackend.wallet.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class WalletEventListenerTest {

    @Mock
    private WalletService walletService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private WalletEventListener walletEventListener;

    private UUID userId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userId = UUID.randomUUID();
    }

    @Test
    void testHandleOrderCreatedEvent_Success() {
        OrderCreatedEvent event = new OrderCreatedEvent(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"), userId, new BigDecimal("50000"));

        walletEventListener.handleOrderCreatedEvent(event);

        verify(walletService, times(1)).payment(any(PaymentRequest.class));

        ArgumentCaptor<PaymentSuccessEvent> eventCaptor = ArgumentCaptor.forClass(PaymentSuccessEvent.class);
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());

        assertEquals(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"), eventCaptor.getValue().getOrderId());
    }

    @Test
    void testHandleOrderCreatedEvent_InsufficientBalance() {
        OrderCreatedEvent event = new OrderCreatedEvent(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"), userId, new BigDecimal("50000"));

        doThrow(new InsufficientBalanceException()).when(walletService).payment(any(PaymentRequest.class));

        walletEventListener.handleOrderCreatedEvent(event);

        ArgumentCaptor<PaymentFailedEvent> eventCaptor = ArgumentCaptor.forClass(PaymentFailedEvent.class);
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());

        assertEquals(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"), eventCaptor.getValue().getOrderId());
        assertEquals("Saldo tidak mencukupi", eventCaptor.getValue().getReason());
    }

    @Test
    void testHandleOrderCreatedEvent_OptimisticLockFailure() {
        OrderCreatedEvent event = new OrderCreatedEvent(
                UUID.fromString("550e8400-e29b-41d4-a716-446655440000"),
                userId,
                new BigDecimal("50000")
        );

        doThrow(new ObjectOptimisticLockingFailureException("wallet", "id"))
                .when(walletService)
                .payment(any(PaymentRequest.class));

        walletEventListener.handleOrderCreatedEvent(event);

        ArgumentCaptor<PaymentFailedEvent> eventCaptor =
                ArgumentCaptor.forClass(PaymentFailedEvent.class);

        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());

        assertEquals(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"), eventCaptor.getValue().getOrderId());
        assertEquals(
                "Sistem sedang sibuk, transaksi dibatalkan",
                eventCaptor.getValue().getReason()
        );
    }

    @Test
    void testHandleOrderCreatedEvent_GenericException() {
        OrderCreatedEvent event = new OrderCreatedEvent(
                UUID.fromString("550e8400-e29b-41d4-a716-446655440000"),
                userId,
                new BigDecimal("50000")
        );

        doThrow(new RuntimeException("unexpected error"))
                .when(walletService)
                .payment(any(PaymentRequest.class));

        walletEventListener.handleOrderCreatedEvent(event);

        ArgumentCaptor<PaymentFailedEvent> eventCaptor =
                ArgumentCaptor.forClass(PaymentFailedEvent.class);

        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());

        assertEquals(UUID.fromString("550e8400-e29b-41d4-a716-446655440000"), eventCaptor.getValue().getOrderId());
        assertEquals(
                "Terjadi kesalahan internal pada sistem pembayaran",
                eventCaptor.getValue().getReason()
        );
    }
}
