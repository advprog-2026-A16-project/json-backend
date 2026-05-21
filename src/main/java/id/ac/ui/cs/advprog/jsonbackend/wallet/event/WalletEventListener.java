package id.ac.ui.cs.advprog.jsonbackend.wallet.event;

import id.ac.ui.cs.advprog.jsonbackend.auth.event.UserRegisteredEvent;
import id.ac.ui.cs.advprog.jsonbackend.wallet.event.model.WalletOutboxEvent;
import id.ac.ui.cs.advprog.jsonbackend.wallet.event.repository.WalletOutboxEventRepository;
import id.ac.ui.cs.advprog.jsonbackend.wallet.exception.InsufficientBalanceException;
import id.ac.ui.cs.advprog.jsonbackend.wallet.exception.WalletNotFoundException;
import id.ac.ui.cs.advprog.jsonbackend.wallet.service.WalletService;
import org.springframework.context.event.EventListener;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class WalletEventListener {

    private final WalletService walletService;
    private final WalletOutboxEventRepository outboxEventRepository;

    public WalletEventListener(WalletService walletService,
                               WalletOutboxEventRepository outboxEventRepository) {
        this.walletService = walletService;
        this.outboxEventRepository = outboxEventRepository;
    }

    @EventListener
    public void handleUserRegisteredEvent(UserRegisteredEvent event) {
        walletService.createWalletIfAbsent(event.userId());
    }

    @EventListener
    public void handleOrderCreatedEvent(OrderCreatedEvent event) {
        processPayment(event.getOrderId(), event.getUserId(), event.getAmount(), UUID.randomUUID().toString());
    }

    @EventListener
    public void handleOrderCancelledEvent(OrderCancelledEvent event) {
        walletService.refundForOrder(event.getUserId(), event.getAmount(), event.getOrderId());
    }

    private void processPayment(UUID orderId, UUID userId, BigDecimal amount, String correlationId) {
        try {
            walletService.paymentForOrder(userId, amount, orderId);
            appendOutboxEvent(
                    WalletEventType.PAYMENT_SUCCESS,
                    orderId,
                    WalletEventPayloadFactory.paymentSuccessPayload(orderId),
                    correlationId
            );

        } catch (InsufficientBalanceException | WalletNotFoundException e) {
            appendPaymentFailedOutboxEvent(orderId, e.getMessage(), correlationId);

        } catch (ObjectOptimisticLockingFailureException e) {
            appendPaymentFailedOutboxEvent(orderId, "Sistem sedang sibuk, transaksi dibatalkan", correlationId);
        } catch (Exception e) {
            appendPaymentFailedOutboxEvent(orderId, "Terjadi kesalahan internal pada sistem pembayaran", correlationId);
        }
    }

    private void appendPaymentFailedOutboxEvent(UUID orderId, String reason, String correlationId) {
        appendOutboxEvent(
                WalletEventType.PAYMENT_FAILED,
                orderId,
                WalletEventPayloadFactory.paymentFailedPayload(orderId, reason),
                correlationId
        );
    }

    private void appendOutboxEvent(WalletEventType eventType, UUID aggregateId, String payload, String correlationId) {
        WalletOutboxEvent outboxEvent = WalletOutboxEvent.builder()
                .eventType(eventType)
                .aggregateId(aggregateId)
                .payload(payload)
                .correlationId(correlationId == null || correlationId.isBlank()
                        ? UUID.randomUUID().toString()
                        : correlationId)
                .build();
        outboxEventRepository.save(outboxEvent);
    }

}
