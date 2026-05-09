package id.ac.ui.cs.advprog.jsonbackend.auth.event.handler;

import id.ac.ui.cs.advprog.jsonbackend.auth.event.OrderCompletedEvent;
import id.ac.ui.cs.advprog.jsonbackend.auth.service.ProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderCompletedEventHandlerTest {

    @Mock
    private ProfileService profileService;

    @InjectMocks
    private OrderCompletedEventHandler orderCompletedEventHandler;

    private UUID orderId;
    private UUID jastiperId;
    private Double rating;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        jastiperId = UUID.randomUUID();
        rating = 5.0;
    }

    @Test
    void handleOrderCompleted_ShouldRecordSuccessfulTransaction() {
        OrderCompletedEvent event = new OrderCompletedEvent(
                orderId,
                jastiperId,
                rating
        );

        orderCompletedEventHandler.handleOrderCompleted(event);

        verify(profileService, times(1))
                .recordSuccessfulTransaction(jastiperId, rating);
    }
}