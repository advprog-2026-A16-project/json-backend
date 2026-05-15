package id.ac.ui.cs.advprog.jsonbackend.order.event.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component("orderOutboxEventScheduler")
public class OutboxEventScheduler {

    private final OutboxEventDispatcher dispatcher;

    public OutboxEventScheduler(OutboxEventDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    // Annotation ini akan membuat fungsi berjalan otomatis setiap 5000 milidetik (5 detik)
    @Scheduled(fixedDelay = 5000)
    public void processOutboxEvents() {
        dispatcher.dispatchPendingEvents();
    }
}
