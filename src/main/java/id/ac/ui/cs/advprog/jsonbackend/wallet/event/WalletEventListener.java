package id.ac.ui.cs.advprog.jsonbackend.wallet.event;

import id.ac.ui.cs.advprog.jsonbackend.wallet.dto.PaymentRequest;
import id.ac.ui.cs.advprog.jsonbackend.wallet.exception.InsufficientBalanceException;
import id.ac.ui.cs.advprog.jsonbackend.wallet.service.WalletService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
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
        try {
            PaymentRequest request = new PaymentRequest();
            request.setUserId(event.getUserId());
            request.setAmount(event.getAmount());

            walletService.payment(request);

            eventPublisher.publishEvent(new PaymentSuccessEvent(event.getOrderId()));

        } catch (InsufficientBalanceException e) {
            eventPublisher.publishEvent(new PaymentFailedEvent(event.getOrderId(), "Saldo tidak mencukupi"));

        } catch (ObjectOptimisticLockingFailureException e) {
            eventPublisher.publishEvent(new PaymentFailedEvent(event.getOrderId(), "Sistem sedang sibuk, transaksi dibatalkan"));
        } catch (Exception e) {
            eventPublisher.publishEvent(new PaymentFailedEvent(event.getOrderId(), "Terjadi kesalahan internal pada sistem pembayaran"));
        }
    }
}
