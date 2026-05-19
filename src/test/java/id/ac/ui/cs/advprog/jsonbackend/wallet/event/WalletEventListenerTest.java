package id.ac.ui.cs.advprog.jsonbackend.wallet.event;

import id.ac.ui.cs.advprog.jsonbackend.auth.event.UserRegisteredEvent;
import id.ac.ui.cs.advprog.jsonbackend.order.event.OrderEventType;
import id.ac.ui.cs.advprog.jsonbackend.order.event.model.OrderOutboxEvent;
import id.ac.ui.cs.advprog.jsonbackend.wallet.exception.InsufficientBalanceException;
import id.ac.ui.cs.advprog.jsonbackend.wallet.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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

    private WalletEventListener walletEventListener;

    private UUID userId;
    private UUID orderId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userId = UUID.randomUUID();
        orderId = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");
        walletEventListener = new WalletEventListener(walletService, eventPublisher);
    }

    @Test
    void testHandleOrderCreatedEvent_Success() {
        OrderCreatedEvent event = new OrderCreatedEvent(orderId, userId, new BigDecimal("50000"));

        walletEventListener.handleOrderCreatedEvent(event);

        verify(walletService, times(1)).paymentForOrder(userId, new BigDecimal("50000"), orderId);

        ArgumentCaptor<id.ac.ui.cs.advprog.jsonbackend.order.event.PaymentSuccessEvent> eventCaptor =
                ArgumentCaptor.forClass(id.ac.ui.cs.advprog.jsonbackend.order.event.PaymentSuccessEvent.class);
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());

        assertEquals(orderId, eventCaptor.getValue().getOrderId());
    }

    @Test
    void testHandleOrderCreatedEvent_InsufficientBalance() {
        OrderCreatedEvent event = new OrderCreatedEvent(orderId, userId, new BigDecimal("50000"));

        doThrow(new InsufficientBalanceException())
                .when(walletService)
                .paymentForOrder(userId, new BigDecimal("50000"), orderId);

        walletEventListener.handleOrderCreatedEvent(event); 

        ArgumentCaptor<id.ac.ui.cs.advprog.jsonbackend.order.event.PaymentFailedEvent> eventCaptor =
                ArgumentCaptor.forClass(id.ac.ui.cs.advprog.jsonbackend.order.event.PaymentFailedEvent.class);
        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());

        assertEquals(orderId, eventCaptor.getValue().getOrderId());
        assertEquals("Saldo tidak mencukupi", eventCaptor.getValue().getReason());
    }

    @Test
    void testHandleOrderCreatedEvent_OptimisticLockFailure() {
        OrderCreatedEvent event = new OrderCreatedEvent(
                orderId,
                userId,
                new BigDecimal("50000")
        );

        doThrow(new ObjectOptimisticLockingFailureException("wallet", "id"))
                .when(walletService)
                .paymentForOrder(userId, new BigDecimal("50000"), orderId);

        walletEventListener.handleOrderCreatedEvent(event);

        ArgumentCaptor<id.ac.ui.cs.advprog.jsonbackend.order.event.PaymentFailedEvent> eventCaptor =
                ArgumentCaptor.forClass(id.ac.ui.cs.advprog.jsonbackend.order.event.PaymentFailedEvent.class);

        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());

        assertEquals(orderId, eventCaptor.getValue().getOrderId());
        assertEquals(
                "Sistem sedang sibuk, transaksi dibatalkan",
                eventCaptor.getValue().getReason()
        );
    }

    @Test
    void testHandleOrderCreatedEvent_GenericException() {
        OrderCreatedEvent event = new OrderCreatedEvent(
                orderId,
                userId,
                new BigDecimal("50000")
        );

        doThrow(new RuntimeException("unexpected error"))
                .when(walletService)
                .paymentForOrder(userId, new BigDecimal("50000"), orderId);

        walletEventListener.handleOrderCreatedEvent(event);

        ArgumentCaptor<id.ac.ui.cs.advprog.jsonbackend.order.event.PaymentFailedEvent> eventCaptor =
                ArgumentCaptor.forClass(id.ac.ui.cs.advprog.jsonbackend.order.event.PaymentFailedEvent.class);

        verify(eventPublisher, times(1)).publishEvent(eventCaptor.capture());

        assertEquals(orderId, eventCaptor.getValue().getOrderId());
        assertEquals(
                "Terjadi kesalahan internal pada sistem pembayaran",
                eventCaptor.getValue().getReason()
        );
    }

    @Test
    void testHandleOrderOutboxEvent_OrderCreated() {
        OrderOutboxEvent event = OrderOutboxEvent.builder()
                .eventType(OrderEventType.ORDER_CREATED)
                .payload(orderPayload())
                .build();

        walletEventListener.handleOrderOutboxEvent(event);

        verify(walletService).paymentForOrder(userId, new BigDecimal("50000"), orderId);
        verify(eventPublisher).publishEvent(any(id.ac.ui.cs.advprog.jsonbackend.order.event.PaymentSuccessEvent.class));
    }

    @Test
    void testHandleOrderOutboxEvent_OrderCancelled() {
        OrderOutboxEvent event = OrderOutboxEvent.builder()
                .eventType(OrderEventType.ORDER_CANCELLED)
                .payload(orderPayload())
                .build();

        walletEventListener.handleOrderOutboxEvent(event);

        verify(walletService).refundForOrder(userId, new BigDecimal("50000"), orderId);
        verify(eventPublisher, never()).publishEvent(any());
    }

    @Test
    void testHandleUserRegisteredEventCreatesWallet() {
        UserRegisteredEvent event = new UserRegisteredEvent(
                UUID.randomUUID(),
                userId,
                "titipers@example.com",
                "TITIPERS",
                "ACTIVE",
                UUID.randomUUID(),
                "titipers",
                0,
                0,
                "correlation-id"
        );

        walletEventListener.handleUserRegisteredEvent(event);

        verify(walletService).createWalletIfAbsent(userId);
    }

    private String orderPayload() {
        return """
                {"orderId":"%s","productId":"%s","quantity":1,"titipersId":"%s","totalPrice":50000}
                """.formatted(orderId, UUID.randomUUID(), userId);
    }
}
