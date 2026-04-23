package id.ac.ui.cs.advprog.jsonbackend.inventory.event.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class OutboxEventScheduler {

    private final OutboxEventDispatcher dispatcher;

    public OutboxEventScheduler(OutboxEventDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Scheduled(fixedDelayString = "${inventory.outbox.dispatch.delay-ms:3000}")
    public void runDispatchCycle() {
        // RED phase skeleton: implementation will be added in GREEN step.
    }
}
