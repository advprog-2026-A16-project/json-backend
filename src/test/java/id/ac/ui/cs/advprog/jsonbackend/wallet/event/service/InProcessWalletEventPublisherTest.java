package id.ac.ui.cs.advprog.jsonbackend.wallet.event.service;

import id.ac.ui.cs.advprog.jsonbackend.order.event.PaymentFailedEvent;
import id.ac.ui.cs.advprog.jsonbackend.order.event.PaymentSuccessEvent;
import id.ac.ui.cs.advprog.jsonbackend.wallet.event.WalletEventPayloadFactory;
import id.ac.ui.cs.advprog.jsonbackend.wallet.event.WalletEventType;
import id.ac.ui.cs.advprog.jsonbackend.wallet.event.model.WalletOutboxEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class InProcessWalletEventPublisherTest {

    private ApplicationEventPublisher applicationEventPublisher;
    private InProcessWalletEventPublisher publisher;
    private UUID orderId;

    @BeforeEach
    void setUp() {
        applicationEventPublisher = mock(ApplicationEventPublisher.class);
        publisher = new InProcessWalletEventPublisher(applicationEventPublisher);
        orderId = UUID.randomUUID();
    }

    @Test
    void publishPaymentSuccessShouldPublishOrderPaymentSuccessEvent() {
        WalletOutboxEvent event = WalletOutboxEvent.builder()
                .eventType(WalletEventType.PAYMENT_SUCCESS)
                .aggregateId(orderId)
                .payload(WalletEventPayloadFactory.paymentSuccessPayload(orderId))
                .correlationId("corr-1")
                .build();

        publisher.publish(event);

        ArgumentCaptor<PaymentSuccessEvent> captor = ArgumentCaptor.forClass(PaymentSuccessEvent.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());
        assertEquals(orderId, captor.getValue().getOrderId());
    }

    @Test
    void publishPaymentFailedShouldPublishOrderPaymentFailedEvent() {
        WalletOutboxEvent event = WalletOutboxEvent.builder()
                .eventType(WalletEventType.PAYMENT_FAILED)
                .aggregateId(orderId)
                .payload(WalletEventPayloadFactory.paymentFailedPayload(orderId, "Saldo tidak mencukupi"))
                .correlationId("corr-2")
                .build();

        publisher.publish(event);

        ArgumentCaptor<PaymentFailedEvent> captor = ArgumentCaptor.forClass(PaymentFailedEvent.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());
        assertEquals(orderId, captor.getValue().getOrderId());
        assertEquals("Saldo tidak mencukupi", captor.getValue().getReason());
    }
}
