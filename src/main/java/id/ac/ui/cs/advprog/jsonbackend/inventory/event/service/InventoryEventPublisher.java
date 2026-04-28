package id.ac.ui.cs.advprog.jsonbackend.inventory.event.service;

import id.ac.ui.cs.advprog.jsonbackend.inventory.event.model.InventoryOutboxEvent;

public interface InventoryEventPublisher {
    void publish(InventoryOutboxEvent event);
}
