package id.ac.ui.cs.advprog.jsonbackend.wallet.event.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.jsonbackend.order.event.PaymentFailedEvent;
import id.ac.ui.cs.advprog.jsonbackend.order.event.PaymentSuccessEvent;
import id.ac.ui.cs.advprog.jsonbackend.wallet.event.model.WalletOutboxEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class InProcessWalletEventPublisher implements WalletEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public InProcessWalletEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publish(WalletOutboxEvent event) {
        switch (event.getEventType()) {
            case PAYMENT_SUCCESS -> applicationEventPublisher.publishEvent(
                    new PaymentSuccessEvent(extractOrderId(event.getPayload()))
            );
            case PAYMENT_FAILED -> applicationEventPublisher.publishEvent(
                    new PaymentFailedEvent(extractOrderId(event.getPayload()), extractReason(event.getPayload()))
            );
        }
    }

    private UUID extractOrderId(String payload) {
        return UUID.fromString(requiredText(payload, "orderId"));
    }

    private String extractReason(String payload) {
        return requiredText(payload, "reason");
    }

    private String requiredText(String payload, String fieldName) {
        try {
            JsonNode root = objectMapper.readTree(payload);
            JsonNode value = root.get(fieldName);
            if (value == null || value.isNull() || !value.isTextual() || value.asText().isBlank()) {
                throw new IllegalArgumentException("Missing " + fieldName);
            }
            return value.asText();
        } catch (JsonProcessingException e) {
            throw new IllegalArgumentException("Invalid wallet event payload", e);
        }
    }
}
