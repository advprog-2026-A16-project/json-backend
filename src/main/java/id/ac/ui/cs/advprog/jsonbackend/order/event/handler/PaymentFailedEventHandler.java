package id.ac.ui.cs.advprog.jsonbackend.order.event.handler;

import id.ac.ui.cs.advprog.jsonbackend.order.event.PaymentFailedEvent;
import id.ac.ui.cs.advprog.jsonbackend.order.event.model.ProcessedEvent;
import id.ac.ui.cs.advprog.jsonbackend.order.event.repository.ProcessedEventRepository;
import id.ac.ui.cs.advprog.jsonbackend.order.model.OrderStatus;
import id.ac.ui.cs.advprog.jsonbackend.order.repository.OrderRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PaymentFailedEventHandler {

    private final OrderRepository orderRepository;
    private final ProcessedEventRepository processedEventRepository;
    private static final String HANDLER_NAME = "PaymentFailedEventHandler";

    public PaymentFailedEventHandler(OrderRepository orderRepository,
                                     ProcessedEventRepository processedEventRepository) {
        this.orderRepository = orderRepository;
        this.processedEventRepository = processedEventRepository;
    }

    @EventListener
    @Transactional
    public void handle(PaymentFailedEvent event) {
        // Cek Idempotency menggunakan orderId sebagai pengganti eventId
        if (processedEventRepository.existsByEventIdAndHandlerName(event.getOrderId(), HANDLER_NAME)) {
            return;
        }

        // Ubah status order menjadi CANCELLED
        orderRepository.findById(event.getOrderId()).ifPresent(order -> {
            order.setStatus(OrderStatus.CANCELLED);
            orderRepository.save(order);
        });

        // Catat bahwa event ini sudah diproses
        ProcessedEvent processedEvent = ProcessedEvent.builder()
                .eventId(event.getOrderId())
                .handlerName(HANDLER_NAME)
                .build();

        processedEventRepository.save(processedEvent);
    }
}