package id.ac.ui.cs.advprog.jsonbackend.wallet.event.service;

import org.junit.jupiter.api.Test;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class WalletOutboxEventSchedulerTest {

    @Test
    void runDispatchCycleShouldDispatchAndRequeue() {
        WalletOutboxEventDispatcher dispatcher = mock(WalletOutboxEventDispatcher.class);
        WalletOutboxEventScheduler scheduler = new WalletOutboxEventScheduler(dispatcher);

        scheduler.runDispatchCycle();

        verify(dispatcher).dispatchPendingEvents();
        verify(dispatcher).requeueFailedEvents();
    }
}
