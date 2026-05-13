package id.ac.ui.cs.advprog.jsonbackend.auth.event.service;

import id.ac.ui.cs.advprog.jsonbackend.auth.event.enums.AuthOutboxEventStatus;
import id.ac.ui.cs.advprog.jsonbackend.auth.event.model.AuthOutboxEvent;
import id.ac.ui.cs.advprog.jsonbackend.auth.event.repository.AuthOutboxEventRepository;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Component
public class AuthOutboxEventDispatcher {
    private static final int MAX_RETRY = 3;
    private static final int MAX_FAILURE_REASON_LENGTH = 500;

    private final AuthOutboxEventRepository outboxEventRepository;
    private final AuthEventPublisher eventPublisher;

    public AuthOutboxEventDispatcher(AuthOutboxEventRepository outboxEventRepository,
                                     AuthEventPublisher eventPublisher) {
        this.outboxEventRepository = outboxEventRepository;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public void dispatchPendingEvents() {
        List<AuthOutboxEvent> pendingEvents =
                outboxEventRepository.findTop50ByStatusOrderByCreatedAtAsc(AuthOutboxEventStatus.PENDING);

        for (AuthOutboxEvent event : pendingEvents) {
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
        List<AuthOutboxEvent> failedEvents =
                outboxEventRepository.findTop50ByStatusOrderByOccurredAtAsc(AuthOutboxEventStatus.FAILED);

        for (AuthOutboxEvent event : failedEvents) {
            if (event.getRetryCount() >= MAX_RETRY) {
                markAsDeadLetter(event);
            } else {
                event.setStatus(AuthOutboxEventStatus.PENDING);
                event.setFailureReason(null);
            }
            outboxEventRepository.save(event);
        }
    }

    private void markAsSent(AuthOutboxEvent event) {
        event.setStatus(AuthOutboxEventStatus.SENT);
        event.setSentAt(LocalDateTime.now());
        event.setFailureReason(null);
    }

    private void markAsDeadLetter(AuthOutboxEvent event) {
        event.setStatus(AuthOutboxEventStatus.DEAD_LETTER);
    }

    private void markRetryableFailure(AuthOutboxEvent event) {
        int nextRetry = event.getRetryCount() + 1;
        event.setRetryCount(nextRetry);
        if (nextRetry >= MAX_RETRY) {
            markAsDeadLetter(event);
        } else {
            event.setStatus(AuthOutboxEventStatus.FAILED);
        }
    }

    private void setFailureReason(AuthOutboxEvent event, Exception ex) {
        String message = ex.getMessage() == null ? "Unknown failure" : ex.getMessage();
        if (message.length() > MAX_FAILURE_REASON_LENGTH) {
            message = message.substring(0, MAX_FAILURE_REASON_LENGTH);
        }
        event.setFailureReason(message);
    }
}