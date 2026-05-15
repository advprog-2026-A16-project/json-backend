package id.ac.ui.cs.advprog.jsonbackend.order.event.handler;

import id.ac.ui.cs.advprog.jsonbackend.order.event.StockReservationFailedEvent;
import id.ac.ui.cs.advprog.jsonbackend.order.event.model.OrderProcessedEvent;
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
class StockReservationFailedEventHandlerTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderProcessedEventRepository processedEventRepository;

    @InjectMocks
    private StockReservationFailedEventHandler handler;

    private StockReservationFailedEvent event;
    private Order order;
    private final String HANDLER_NAME = "StockReservationFailedEventHandler";

    @BeforeEach
    void setUp() {
        UUID eventId = UUID.randomUUID();
        UUID orderId = UUID.randomUUID();

        event = new StockReservationFailedEvent(eventId, orderId, "Stok Habis");

        order = new Order();
        order.setId(orderId);
        order.setStatus(OrderStatus.PAID);
    }

    @Test
    void testHandleEventSuccess() {
        // Mock: Event belum pernah diproses
        when(processedEventRepository.existsByEventIdAndHandlerName(event.getEventId(), HANDLER_NAME))
                .thenReturn(false);
        when(orderRepository.findById(event.getOrderId())).thenReturn(Optional.of(order));

        handler.handle(event);

        // Pastikan status order jadi CANCELLED
        assertEquals(OrderStatus.CANCELLED, order.getStatus());
        verify(orderRepository).save(order);
        verify(processedEventRepository).save(any(OrderProcessedEvent.class));
    }

    @Test
    void testHandleEventIdempotent() {
        // Mock: Event SUDAH pernah diproses
        when(processedEventRepository.existsByEventIdAndHandlerName(event.getEventId(), HANDLER_NAME))
                .thenReturn(true);

        handler.handle(event);

        // Pastikan tidak ada perubahan data (idempotent)
        verify(orderRepository, never()).save(any());
        verify(processedEventRepository, never()).save(any());
    }
}