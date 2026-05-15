package id.ac.ui.cs.advprog.jsonbackend.order.event.service;

import id.ac.ui.cs.advprog.jsonbackend.order.event.model.OrderOutboxEvent;

public interface OrderEventPublisher {
    void publish(OrderOutboxEvent event);
}