package id.ac.ui.cs.advprog.jsonbackend.inventory.event.service;

import id.ac.ui.cs.advprog.jsonbackend.inventory.event.OutboxEventStatus;
import id.ac.ui.cs.advprog.jsonbackend.inventory.event.model.InventoryOutboxEvent;
import id.ac.ui.cs.advprog.jsonbackend.inventory.event.repository.InventoryOutboxEventRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

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
        List<InventoryOutboxEvent> pendingEvents =
                outboxEventRepository.findTop50ByStatusOrderByCreatedAtAsc(OutboxEventStatus.PENDING);

        for (InventoryOutboxEvent event : pendingEvents) {
            try {
                eventPublisher.publish(event);
                event.setStatus(OutboxEventStatus.SENT);
                event.setSentAt(LocalDateTime.now());
            } catch (Exception ex) {
                event.setStatus(OutboxEventStatus.FAILED);
                event.setRetryCount(event.getRetryCount() + 1);
            }
            outboxEventRepository.save(event);
        }
    }
}
