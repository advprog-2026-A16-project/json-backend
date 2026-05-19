package id.ac.ui.cs.advprog.jsonbackend.wallet.event.service;

import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
public class WalletOutboxEventScheduler {

    private final WalletOutboxEventDispatcher dispatcher;

    public WalletOutboxEventScheduler(WalletOutboxEventDispatcher dispatcher) {
        this.dispatcher = dispatcher;
    }

    @Scheduled(fixedDelayString = "${wallet.outbox.dispatch.delay-ms:3000}")
    public void runDispatchCycle() {
        dispatcher.dispatchPendingEvents();
        dispatcher.requeueFailedEvents();
    }
}
