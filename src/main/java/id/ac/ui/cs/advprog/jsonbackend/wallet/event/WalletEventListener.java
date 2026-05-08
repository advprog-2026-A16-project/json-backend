package id.ac.ui.cs.advprog.jsonbackend.wallet.event;

import id.ac.ui.cs.advprog.jsonbackend.wallet.service.WalletService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class WalletEventListener {

    private final WalletService walletService;
    private final ApplicationEventPublisher eventPublisher;

    public WalletEventListener(WalletService walletService, ApplicationEventPublisher eventPublisher) {
        this.walletService = walletService;
        this.eventPublisher = eventPublisher;
    }

    @EventListener
    public void handleOrderCreatedEvent(OrderCreatedEvent event) {
    }
}
