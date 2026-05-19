package id.ac.ui.cs.advprog.jsonbackend.wallet.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.jsonbackend.auth.event.UserRegisteredEvent;
import id.ac.ui.cs.advprog.jsonbackend.order.event.OrderEventType;
import id.ac.ui.cs.advprog.jsonbackend.order.event.model.OrderOutboxEvent;
import id.ac.ui.cs.advprog.jsonbackend.wallet.exception.InsufficientBalanceException;
import id.ac.ui.cs.advprog.jsonbackend.wallet.exception.WalletNotFoundException;
import id.ac.ui.cs.advprog.jsonbackend.wallet.service.WalletService;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.context.event.EventListener;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
public class WalletEventListener {

    private final WalletService walletService;
    private final ApplicationEventPublisher eventPublisher;
    private final ObjectMapper objectMapper;

    public WalletEventListener(WalletService walletService,
                               ApplicationEventPublisher eventPublisher) {
        this.walletService = walletService;
        this.eventPublisher = eventPublisher;
        this.objectMapper = new ObjectMapper();
    }

    @EventListener
    public void handleUserRegisteredEvent(UserRegisteredEvent event) {
        walletService.createWalletIfAbsent(event.userId());
    }

    @EventListener
    public void handleOrderOutboxEvent(OrderOutboxEvent event) {
        if (event.getEventType() == OrderEventType.ORDER_CREATED) {
            OrderWalletPayload payload = parseOrderPayload(event.getPayload());
            processPayment(payload.orderId(), payload.userId(), payload.amount());
        } else if (event.getEventType() == OrderEventType.ORDER_CANCELLED) {
            OrderWalletPayload payload = parseOrderPayload(event.getPayload());
            walletService.refundForOrder(payload.userId(), payload.amount(), payload.orderId());
        }
    }

    @EventListener
    public void handleOrderCreatedEvent(OrderCreatedEvent event) {
        processPayment(event.getOrderId(), event.getUserId(), event.getAmount());
    }

    private void processPayment(UUID orderId, UUID userId, BigDecimal amount) {
        try {
            walletService.paymentForOrder(userId, amount, orderId);

            eventPublisher.publishEvent(new id.ac.ui.cs.advprog.jsonbackend.order.event.PaymentSuccessEvent(orderId));

        } catch (InsufficientBalanceException | WalletNotFoundException e) {
            eventPublisher.publishEvent(new id.ac.ui.cs.advprog.jsonbackend.order.event.PaymentFailedEvent(orderId, e.getMessage()));

        } catch (ObjectOptimisticLockingFailureException e) {
            eventPublisher.publishEvent(new id.ac.ui.cs.advprog.jsonbackend.order.event.PaymentFailedEvent(orderId, "Sistem sedang sibuk, transaksi dibatalkan"));
        } catch (Exception e) {
            eventPublisher.publishEvent(new id.ac.ui.cs.advprog.jsonbackend.order.event.PaymentFailedEvent(orderId, "Terjadi kesalahan internal pada sistem pembayaran"));
        }
    }

    private OrderWalletPayload parseOrderPayload(String payload) {
        try {
            JsonNode root = objectMapper.readTree(payload);
            UUID orderId = UUID.fromString(requiredText(root, "orderId"));
            UUID userId = UUID.fromString(requiredText(root, "titipersId"));
            BigDecimal amount = required(root, "totalPrice").decimalValue();

            return new OrderWalletPayload(orderId, userId, amount);
        } catch (JsonProcessingException | IllegalArgumentException e) {
            throw new IllegalArgumentException("Invalid wallet order payload", e);
        }
    }

    private String requiredText(JsonNode root, String fieldName) {
        JsonNode value = required(root, fieldName);
        if (!value.isTextual() || value.asText().isBlank()) {
            throw new IllegalArgumentException("Missing " + fieldName);
        }
        return value.asText();
    }

    private JsonNode required(JsonNode root, String fieldName) {
        JsonNode value = root.get(fieldName);
        if (value == null || value.isNull()) {
            throw new IllegalArgumentException("Missing " + fieldName);
        }
        return value;
    }

    private record OrderWalletPayload(UUID orderId, UUID userId, BigDecimal amount) {
    }
}
