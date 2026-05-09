package id.ac.ui.cs.advprog.jsonbackend.auth.event.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class AuthOutboxEventScheduler {
    private final AuthOutboxEventDispatcher dispatcher;

    public AuthOutboxEventScheduler(AuthOutboxEventDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Scheduled(fixedDelayString = "${auth.outbox.dispatch.delay-ms:3000}")
    public void runDispatchCycle() {
        dispatcher.dispatchPendingEvents();
        dispatcher.requeueFailedEvents();
    }
}