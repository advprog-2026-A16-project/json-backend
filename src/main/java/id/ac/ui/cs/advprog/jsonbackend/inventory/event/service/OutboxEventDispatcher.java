package id.ac.ui.cs.advprog.jsonbackend.inventory.event.service;

import id.ac.ui.cs.advprog.jsonbackend.inventory.event.repository.InventoryOutboxEventRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class OutboxEventDispatcher {

    private final InventoryOutboxEventRepository outboxEventRepository;
    private final InventoryEventPublisher eventPublisher;

    public OutboxEventDispatcher(InventoryOutboxEventRepository outboxEventRepository,
                                 InventoryEventPublisher eventPublisher) {
        this.outboxEventRepository = outboxEventRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public void dispatchPendingEvents() {
        // RED phase skeleton: implementation will be added in GREEN step.
    }
}
