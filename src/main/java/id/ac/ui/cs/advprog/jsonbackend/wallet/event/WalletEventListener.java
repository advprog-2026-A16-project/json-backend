package id.ac.ui.cs.advprog.jsonbackend.wallet.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.jsonbackend.auth.event.UserRegisteredEvent;
import id.ac.ui.cs.advprog.jsonbackend.order.event.OrderEventType;
import id.ac.ui.cs.advprog.jsonbackend.order.event.model.OrderOutboxEvent;
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
    private final ObjectMapper objectMapper;

    public WalletEventListener(WalletService walletService,
                               WalletOutboxEventRepository outboxEventRepository) {
        this.walletService = walletService;
        this.outboxEventRepository = outboxEventRepository;
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
            processPayment(payload.orderId(), payload.userId(), payload.amount(), event.getCorrelationId());
        } else if (event.getEventType() == OrderEventType.ORDER_CANCELLED) {
            OrderWalletPayload payload = parseOrderPayload(event.getPayload());
            walletService.refundForOrder(payload.userId(), payload.amount(), payload.orderId());
        }
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
