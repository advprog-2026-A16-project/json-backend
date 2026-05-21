package id.ac.ui.cs.advprog.jsonbackend.order.event.handler;

import id.ac.ui.cs.advprog.jsonbackend.order.event.PaymentFailedEvent;
import id.ac.ui.cs.advprog.jsonbackend.order.event.OrderEventPayloadFactory;
import id.ac.ui.cs.advprog.jsonbackend.order.event.OrderEventType;
import id.ac.ui.cs.advprog.jsonbackend.order.event.model.OrderOutboxEvent;
import id.ac.ui.cs.advprog.jsonbackend.order.event.model.OrderProcessedEvent;
import id.ac.ui.cs.advprog.jsonbackend.order.event.repository.OrderOutboxEventRepository;
import id.ac.ui.cs.advprog.jsonbackend.order.event.repository.OrderProcessedEventRepository;
import id.ac.ui.cs.advprog.jsonbackend.order.model.OrderStatus;
import id.ac.ui.cs.advprog.jsonbackend.order.repository.OrderRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
public class PaymentFailedEventHandler {

    private final OrderRepository orderRepository;
    private final OrderProcessedEventRepository processedEventRepository;
    private final OrderOutboxEventRepository outboxEventRepository;
    private static final String HANDLER_NAME = "PaymentFailedEventHandler";

    public PaymentFailedEventHandler(OrderRepository orderRepository,
                                     OrderProcessedEventRepository processedEventRepository,
                                     OrderOutboxEventRepository outboxEventRepository) {
        this.orderRepository = orderRepository;
        this.processedEventRepository = processedEventRepository;
        this.outboxEventRepository = outboxEventRepository;
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
            appendStockReleaseEvent(order.getId(), order.getProductId(), order.getQuantity());
        });

        // Catat bahwa event ini sudah diproses
        OrderProcessedEvent processedEvent = OrderProcessedEvent.builder()
                .eventId(event.getOrderId())
                .handlerName(HANDLER_NAME)
                .build();

        processedEventRepository.save(processedEvent);
    }

    private void appendStockReleaseEvent(UUID orderId, UUID productId, int quantity) {
        String payload = OrderEventPayloadFactory.stockReleaseRequestedPayload(orderId, productId, quantity);
        OrderOutboxEvent outboxEvent = OrderOutboxEvent.builder()
                .eventType(OrderEventType.STOCK_RELEASE_REQUESTED)
                .aggregateId(orderId)
                .payload(payload)
                .correlationId(UUID.randomUUID().toString())
                .build();
        outboxEventRepository.save(outboxEvent);
    }
}
