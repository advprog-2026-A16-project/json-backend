package id.ac.ui.cs.advprog.jsonbackend.wallet.event.service;

import id.ac.ui.cs.advprog.jsonbackend.wallet.event.WalletOutboxEventStatus;
import id.ac.ui.cs.advprog.jsonbackend.wallet.event.model.WalletOutboxEvent;
import id.ac.ui.cs.advprog.jsonbackend.wallet.event.repository.WalletOutboxEventRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class WalletOutboxEventDispatcher {
    private static final int MAX_RETRY = 3;
    private static final int MAX_FAILURE_REASON_LENGTH = 500;

    private final WalletOutboxEventRepository outboxEventRepository;
    private final WalletEventPublisher eventPublisher;

    public WalletOutboxEventDispatcher(WalletOutboxEventRepository outboxEventRepository,
                                       WalletEventPublisher eventPublisher) {
        this.outboxEventRepository = outboxEventRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public void dispatchPendingEvents() {
        List<WalletOutboxEvent> pendingEvents =
                outboxEventRepository.findTop50ByStatusOrderByCreatedAtAsc(WalletOutboxEventStatus.PENDING);

        for (WalletOutboxEvent event : pendingEvents) {
            try {
                eventPublisher.publish(event);
                markAsSent(event);
            } catch (Exception ex) {
                markRetryableFailure(event);
                setFailureReason(event, ex);
            }
            outboxEventRepository.save(event);
        }
    }

    @Transactional
    public void requeueFailedEvents() {
        List<WalletOutboxEvent> failedEvents =
                outboxEventRepository.findTop50ByStatusOrderByOccurredAtAsc(WalletOutboxEventStatus.FAILED);

        for (WalletOutboxEvent event : failedEvents) {
            if (isRetryLimitReached(event)) {
                markAsDeadLetter(event);
            } else {
                event.setStatus(WalletOutboxEventStatus.PENDING);
                event.setFailureReason(null);
            }
            outboxEventRepository.save(event);
        }
    }

    private void markAsSent(WalletOutboxEvent event) {
        event.setStatus(WalletOutboxEventStatus.SENT);
        event.setSentAt(LocalDateTime.now());
        event.setFailureReason(null);
    }

    private void markRetryableFailure(WalletOutboxEvent event) {
        int nextRetry = event.getRetryCount() + 1;
        event.setRetryCount(nextRetry);
        if (nextRetry >= MAX_RETRY) {
            markAsDeadLetter(event);
        } else {
            event.setStatus(WalletOutboxEventStatus.FAILED);
        }
    }

    private void markAsDeadLetter(WalletOutboxEvent event) {
        event.setStatus(WalletOutboxEventStatus.DEAD_LETTER);
    }

    private boolean isRetryLimitReached(WalletOutboxEvent event) {
        return event.getRetryCount() >= MAX_RETRY;
    }

    private void setFailureReason(WalletOutboxEvent event, Exception ex) {
        String message = ex.getMessage() == null ? "Unknown failure" : ex.getMessage();
        event.setFailureReason(truncateFailureReason(message));
    }

    private String truncateFailureReason(String message) {
        if (message.length() <= MAX_FAILURE_REASON_LENGTH) {
            return message;
        }
        return message.substring(0, MAX_FAILURE_REASON_LENGTH);
    }
}
