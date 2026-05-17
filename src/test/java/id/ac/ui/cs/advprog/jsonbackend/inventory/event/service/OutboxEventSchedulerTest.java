package id.ac.ui.cs.advprog.jsonbackend.inventory.event.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OutboxEventSchedulerTest {

    @Mock
    private OutboxEventDispatcher dispatcher;

    @InjectMocks
    private OutboxEventScheduler scheduler;

    @Test
    void runDispatchCycleShouldInvokeDispatchAndRequeue() {
        scheduler.runDispatchCycle();

        verify(dispatcher, times(1)).dispatchPendingEvents();
        verify(dispatcher, times(1)).requeueFailedEvents();
    }
}
