package id.ac.ui.cs.advprog.jsonbackend.inventory.event.service;

import id.ac.ui.cs.advprog.jsonbackend.inventory.event.NonRetryableInventoryEventException;
import id.ac.ui.cs.advprog.jsonbackend.inventory.event.OutboxEventStatus;
import id.ac.ui.cs.advprog.jsonbackend.inventory.event.model.InventoryOutboxEvent;
import id.ac.ui.cs.advprog.jsonbackend.inventory.event.repository.InventoryOutboxEventRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class OutboxEventDispatcher {
    private static final int MAX_RETRY = 3;

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
                markAsSent(event);
            } catch (NonRetryableInventoryEventException ex) {
                markAsDeadLetter(event);
            } catch (Exception ex) {
                markRetryableFailure(event);
            }
            outboxEventRepository.save(event);
        }
    }

    @Transactional
    public void requeueFailedEvents() {
        List<InventoryOutboxEvent> failedEvents =
                outboxEventRepository.findTop50ByStatusOrderByOccurredAtAsc(OutboxEventStatus.FAILED);

        for (InventoryOutboxEvent event : failedEvents) {
            if (isRetryLimitReached(event)) {
                markAsDeadLetter(event);
            } else {
                event.setStatus(OutboxEventStatus.PENDING);
            }
            outboxEventRepository.save(event);
        }
    }

    private void markAsSent(InventoryOutboxEvent event) {
        event.setStatus(OutboxEventStatus.SENT);
        event.setSentAt(LocalDateTime.now());
    }

    private void markAsDeadLetter(InventoryOutboxEvent event) {
        event.setStatus(OutboxEventStatus.DEAD_LETTER);
    }

    private void markRetryableFailure(InventoryOutboxEvent event) {
        int nextRetry = event.getRetryCount() + 1;
        event.setRetryCount(nextRetry);
        if (nextRetry >= MAX_RETRY) {
            markAsDeadLetter(event);
        } else {
            event.setStatus(OutboxEventStatus.FAILED);
        }
    }

    private boolean isRetryLimitReached(InventoryOutboxEvent event) {
        return event.getRetryCount() >= MAX_RETRY;
    }
}
