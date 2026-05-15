package id.ac.ui.cs.advprog.jsonbackend.order.event.handler;

import id.ac.ui.cs.advprog.jsonbackend.order.event.StockReservationFailedEvent;
import id.ac.ui.cs.advprog.jsonbackend.order.event.model.OrderProcessedEvent;
import id.ac.ui.cs.advprog.jsonbackend.order.event.repository.OrderProcessedEventRepository;
import id.ac.ui.cs.advprog.jsonbackend.order.model.OrderStatus;
import id.ac.ui.cs.advprog.jsonbackend.order.repository.OrderRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class StockReservationFailedEventHandler {

    private final OrderRepository orderRepository;
    private final OrderProcessedEventRepository processedEventRepository;
    private static final String HANDLER_NAME = "StockReservationFailedEventHandler";

    public StockReservationFailedEventHandler(OrderRepository orderRepository,
                                              OrderProcessedEventRepository processedEventRepository) {
        this.orderRepository = orderRepository;
        this.processedEventRepository = processedEventRepository;
    }

    @EventListener
    @Transactional
    public void handle(StockReservationFailedEvent event) {
        // Cek Idempotency: Jika event ini sudah pernah diproses, hentikan eksekusi
        if (processedEventRepository.existsByEventIdAndHandlerName(event.getEventId(), HANDLER_NAME)) {
            return;
        }

        // Ubah status order menjadi CANCELLED jika order ditemukan
        orderRepository.findById(event.getOrderId()).ifPresent(order -> {
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
        });

        // Catat di database bahwa event ini sudah selesai diproses
        OrderProcessedEvent processedEvent = OrderProcessedEvent.builder()
                .eventId(event.getEventId())
                .handlerName(HANDLER_NAME)
                .build();

        processedEventRepository.save(processedEvent);
    }
}