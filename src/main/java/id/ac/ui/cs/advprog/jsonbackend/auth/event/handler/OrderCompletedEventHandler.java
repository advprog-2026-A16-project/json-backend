package id.ac.ui.cs.advprog.jsonbackend.auth.event.handler;

import id.ac.ui.cs.advprog.jsonbackend.auth.event.OrderCompletedEvent;
import id.ac.ui.cs.advprog.jsonbackend.auth.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderCompletedEventHandler {

    private final ProfileService profileService;

    @EventListener
    public void handleOrderCompleted(OrderCompletedEvent event) {
        profileService.recordSuccessfulTransaction(event.jastiperId(), event.rating());
    }
}