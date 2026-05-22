package id.ac.ui.cs.advprog.jsonbackend.order.event.handler;

import id.ac.ui.cs.advprog.jsonbackend.order.event.PaymentFailedEvent;
import id.ac.ui.cs.advprog.jsonbackend.order.event.OrderEventType;
import id.ac.ui.cs.advprog.jsonbackend.order.event.model.OrderOutboxEvent;
import id.ac.ui.cs.advprog.jsonbackend.order.event.model.OrderProcessedEvent;
import id.ac.ui.cs.advprog.jsonbackend.order.event.repository.OrderOutboxEventRepository;
import id.ac.ui.cs.advprog.jsonbackend.order.event.repository.OrderProcessedEventRepository;
import id.ac.ui.cs.advprog.jsonbackend.order.model.Order;
import id.ac.ui.cs.advprog.jsonbackend.order.model.OrderStatus;
import id.ac.ui.cs.advprog.jsonbackend.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentFailedEventHandlerTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderProcessedEventRepository processedEventRepository;

    @Mock
    private OrderOutboxEventRepository outboxEventRepository;

    @InjectMocks
    private PaymentFailedEventHandler handler;

    private PaymentFailedEvent event;
    private Order order;
    private final String HANDLER_NAME = "PaymentFailedEventHandler";

    @BeforeEach
    void setUp() {
        UUID orderId = UUID.randomUUID();
        event = new PaymentFailedEvent(orderId, "Saldo tidak mencukupi");

        order = new Order();
        order.setId(orderId);
        order.setProductId(UUID.randomUUID());
        order.setQuantity(2);
        order.setStatus(OrderStatus.PAID);
    }

    @Test
    void testHandleEventSuccess() {
        // Mocking: Event belum pernah diproses. Kita menggunakan orderId sebagai eventId pengganti.
        when(processedEventRepository.existsByEventIdAndHandlerName(event.getOrderId(), HANDLER_NAME))
                .thenReturn(false);
        when(orderRepository.findById(event.getOrderId())).thenReturn(Optional.of(order));

        handler.handle(event);

        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        verify(orderRepository).save(order);
        verify(processedEventRepository).save(any(OrderProcessedEvent.class));
        verify(outboxEventRepository).save(argThat(outboxEvent ->
                outboxEvent.getEventType() == OrderEventType.STOCK_RELEASE_REQUESTED
                        && outboxEvent.getAggregateId().equals(order.getId())
                        && outboxEvent.getPayload().contains(order.getProductId().toString())
                        && outboxEvent.getPayload().contains("\"quantity\":2")
        ));
    }

    @Test
    void testHandleEventIdempotent() {
        when(processedEventRepository.existsByEventIdAndHandlerName(event.getOrderId(), HANDLER_NAME))
                .thenReturn(true);

        handler.handle(event);

        verify(orderRepository, never()).save(any());
        verify(processedEventRepository, never()).save(any());
        verify(outboxEventRepository, never()).save(any());
    }
}
