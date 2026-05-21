package id.ac.ui.cs.advprog.jsonbackend.order.event.service;

import id.ac.ui.cs.advprog.jsonbackend.auth.event.OrderCompletedEvent;
import id.ac.ui.cs.advprog.jsonbackend.inventory.event.StockReleaseRequestedEvent;
import id.ac.ui.cs.advprog.jsonbackend.inventory.event.StockReservationRequestedEvent;
import id.ac.ui.cs.advprog.jsonbackend.inventory.exception.InsufficientStockException;
import id.ac.ui.cs.advprog.jsonbackend.order.event.OrderEventPayloadFactory;
import id.ac.ui.cs.advprog.jsonbackend.order.event.OrderEventType;
import id.ac.ui.cs.advprog.jsonbackend.order.event.StockReservationFailedEvent;
import id.ac.ui.cs.advprog.jsonbackend.order.event.model.OrderOutboxEvent;
import id.ac.ui.cs.advprog.jsonbackend.wallet.event.OrderCancelledEvent;
import id.ac.ui.cs.advprog.jsonbackend.wallet.event.OrderCreatedEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InProcessOrderEventPublisherTest {

    @Mock
    private ApplicationEventPublisher applicationEventPublisher;

    private InProcessOrderEventPublisher publisher;

    @BeforeEach
    void setUp() {
        publisher = new InProcessOrderEventPublisher(applicationEventPublisher);
    }

    @Test
    void publishShouldRouteOrderCreatedToWalletEvent() {
        UUID orderId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID titipersId = UUID.randomUUID();
        OrderOutboxEvent event = outboxEvent(
                OrderEventType.ORDER_CREATED,
                OrderEventPayloadFactory.orderCreatedPayload(orderId, productId, 2, titipersId, new BigDecimal("150000"))
        );

        publisher.publish(event);

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());
        OrderCreatedEvent routedEvent = assertInstanceOf(OrderCreatedEvent.class, captor.getValue());
        assertEquals(orderId, routedEvent.getOrderId());
        assertEquals(titipersId, routedEvent.getUserId());
        assertEquals(new BigDecimal("150000"), routedEvent.getAmount());
    }

    @Test
    void publishShouldRouteOrderCancelledToWalletRefundEvent() {
        UUID orderId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID titipersId = UUID.randomUUID();
        OrderOutboxEvent event = outboxEvent(
                OrderEventType.ORDER_CANCELLED,
                OrderEventPayloadFactory.orderCancelledPayload(orderId, productId, 1, titipersId, new BigDecimal("50000"))
        );

        publisher.publish(event);

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());
        OrderCancelledEvent routedEvent = assertInstanceOf(OrderCancelledEvent.class, captor.getValue());
        assertEquals(orderId, routedEvent.getOrderId());
        assertEquals(titipersId, routedEvent.getUserId());
        assertEquals(new BigDecimal("50000"), routedEvent.getAmount());
    }

    @Test
    void publishShouldRouteStockReservationToInventoryEvent() {
        UUID orderId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        OrderOutboxEvent event = outboxEvent(
                eventId,
                OrderEventType.STOCK_RESERVATION_REQUESTED,
                OrderEventPayloadFactory.stockReservationRequestedPayload(orderId, productId, 3)
        );

        publisher.publish(event);

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());
        StockReservationRequestedEvent routedEvent = assertInstanceOf(StockReservationRequestedEvent.class, captor.getValue());
        assertEquals(eventId, routedEvent.eventId());
        assertEquals(productId, routedEvent.productId());
        assertEquals(3, routedEvent.quantity());
        assertEquals("corr-123", routedEvent.correlationId());
    }

    @Test
    void publishShouldEmitFailureEventAndRethrowWhenStockReservationFails() {
        UUID orderId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        OrderOutboxEvent event = outboxEvent(
                eventId,
                OrderEventType.STOCK_RESERVATION_REQUESTED,
                OrderEventPayloadFactory.stockReservationRequestedPayload(orderId, productId, 2)
        );

        doAnswer(invocation -> {
            Object argument = invocation.getArgument(0);
            if (argument instanceof StockReservationRequestedEvent) {
                throw new InsufficientStockException("Insufficient stock");
            }
            return null;
        }).when(applicationEventPublisher).publishEvent(any(Object.class));

        InsufficientStockException exception =
                assertThrows(InsufficientStockException.class, () -> publisher.publish(event));

        assertEquals("Insufficient stock", exception.getMessage());

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(applicationEventPublisher, times(2)).publishEvent(captor.capture());
        StockReservationFailedEvent failureEvent =
                assertInstanceOf(StockReservationFailedEvent.class, captor.getAllValues().get(1));
        assertEquals(eventId, failureEvent.getEventId());
        assertEquals(orderId, failureEvent.getOrderId());
        assertEquals("Insufficient stock", failureEvent.getReason());
    }

    @Test
    void publishShouldRouteStockReleaseToInventoryEvent() {
        UUID orderId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        UUID eventId = UUID.randomUUID();
        OrderOutboxEvent event = outboxEvent(
                eventId,
                OrderEventType.STOCK_RELEASE_REQUESTED,
                OrderEventPayloadFactory.stockReleaseRequestedPayload(orderId, productId, 4)
        );

        publisher.publish(event);

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());
        StockReleaseRequestedEvent routedEvent = assertInstanceOf(StockReleaseRequestedEvent.class, captor.getValue());
        assertEquals(eventId, routedEvent.eventId());
        assertEquals(productId, routedEvent.productId());
        assertEquals(4, routedEvent.quantity());
        assertEquals("corr-123", routedEvent.correlationId());
    }

    @Test
    void publishShouldRouteOrderRatedToAuthEvent() {
        UUID orderId = UUID.randomUUID();
        UUID jastiperId = UUID.randomUUID();
        OrderOutboxEvent event = outboxEvent(
                OrderEventType.ORDER_RATED,
                OrderEventPayloadFactory.orderRatedPayload(orderId, jastiperId, 5, 4)
        );

        publisher.publish(event);

        ArgumentCaptor<Object> captor = ArgumentCaptor.forClass(Object.class);
        verify(applicationEventPublisher).publishEvent(captor.capture());
        OrderCompletedEvent routedEvent = assertInstanceOf(OrderCompletedEvent.class, captor.getValue());
        assertEquals(orderId, routedEvent.orderId());
        assertEquals(jastiperId, routedEvent.jastiperId());
        assertEquals(5.0, routedEvent.rating());
    }

    @Test
    void publishShouldRejectInvalidPayload() {
        OrderOutboxEvent event = outboxEvent(OrderEventType.ORDER_CREATED, "{\"orderId\":\"broken\"}");

        assertThrows(IllegalArgumentException.class, () -> publisher.publish(event));
    }

    private OrderOutboxEvent outboxEvent(OrderEventType eventType, String payload) {
        return outboxEvent(UUID.randomUUID(), eventType, payload);
    }

    private OrderOutboxEvent outboxEvent(UUID eventId, OrderEventType eventType, String payload) {
        OrderOutboxEvent event = new OrderOutboxEvent();
        event.setEventId(eventId);
        event.setEventType(eventType);
        event.setPayload(payload);
        event.setCorrelationId("corr-123");
        return event;
    }
}
