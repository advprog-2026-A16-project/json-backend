package id.ac.ui.cs.advprog.jsonbackend.order.event.handler;

import id.ac.ui.cs.advprog.jsonbackend.order.event.PaymentSuccessEvent;
import id.ac.ui.cs.advprog.jsonbackend.order.event.model.ProcessedEvent;
import id.ac.ui.cs.advprog.jsonbackend.order.event.repository.ProcessedEventRepository;
import id.ac.ui.cs.advprog.jsonbackend.order.model.OrderStatus;
import id.ac.ui.cs.advprog.jsonbackend.order.repository.OrderRepository;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class PaymentSuccessEventHandler {

    private final OrderRepository orderRepository;
    private final ProcessedEventRepository processedEventRepository;
    private static final String HANDLER_NAME = "PaymentSuccessEventHandler";

    public PaymentSuccessEventHandler(OrderRepository orderRepository,
                                      ProcessedEventRepository processedEventRepository) {
        this.orderRepository = orderRepository;
        this.processedEventRepository = processedEventRepository;
    }

    @EventListener
    @Transactional
    public void handle(PaymentSuccessEvent event) {
        // Cek Idempotency
        if (processedEventRepository.existsByEventIdAndHandlerName(event.getOrderId(), HANDLER_NAME)) {
            return;
        }

        // Pastikan order tetap berstatus PAID
        orderRepository.findById(event.getOrderId()).ifPresent(order -> {
            if (order.getStatus() != OrderStatus.PAID) {
                order.setStatus(OrderStatus.PAID);
                orderRepository.save(order);
            }
        });

        ProcessedEvent processedEvent = ProcessedEvent.builder()
                .eventId(event.getOrderId())
                .handlerName(HANDLER_NAME)
                .build();

        processedEventRepository.save(processedEvent);
    }
}