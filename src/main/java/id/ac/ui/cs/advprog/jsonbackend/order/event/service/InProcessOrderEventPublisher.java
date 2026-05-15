package id.ac.ui.cs.advprog.jsonbackend.order.event.service;

import id.ac.ui.cs.advprog.jsonbackend.order.event.model.OrderOutboxEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
public class InProcessOrderEventPublisher implements OrderEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public InProcessOrderEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publish(OrderOutboxEvent event) {
        // Meneruskan event ke sistem antarmuka event bawaan Spring
        applicationEventPublisher.publishEvent(event);
    }
}