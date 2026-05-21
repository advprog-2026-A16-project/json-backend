package id.ac.ui.cs.advprog.jsonbackend.order.event.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import id.ac.ui.cs.advprog.jsonbackend.auth.event.OrderCompletedEvent;
import id.ac.ui.cs.advprog.jsonbackend.inventory.event.StockReleaseRequestedEvent;
import id.ac.ui.cs.advprog.jsonbackend.inventory.event.StockReservationRequestedEvent;
import id.ac.ui.cs.advprog.jsonbackend.order.event.OrderEventType;
import id.ac.ui.cs.advprog.jsonbackend.order.event.StockReservationFailedEvent;
import id.ac.ui.cs.advprog.jsonbackend.order.event.model.OrderOutboxEvent;
import id.ac.ui.cs.advprog.jsonbackend.wallet.event.OrderCancelledEvent;
import id.ac.ui.cs.advprog.jsonbackend.wallet.event.OrderCreatedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.UUID;

@Component
public class InProcessOrderEventPublisher implements OrderEventPublisher {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final ApplicationEventPublisher applicationEventPublisher;

    public InProcessOrderEventPublisher(ApplicationEventPublisher applicationEventPublisher) {
        this.applicationEventPublisher = applicationEventPublisher;
    }

    @Override
    public void publish(OrderOutboxEvent event) {
        JsonNode payload = parsePayload(event.getPayload());
        OrderEventType eventType = event.getEventType();

        switch (eventType) {
            case ORDER_CREATED -> applicationEventPublisher.publishEvent(
                    new OrderCreatedEvent(
                            readUuid(payload, "orderId"),
                            readUuid(payload, "titipersId"),
                            readBigDecimal(payload, "totalPrice")
                    )
            );
            case ORDER_CANCELLED -> applicationEventPublisher.publishEvent(
                    new OrderCancelledEvent(
                            readUuid(payload, "orderId"),
                            readUuid(payload, "titipersId"),
                            readBigDecimal(payload, "totalPrice")
                    )
            );
            case ORDER_RATED -> applicationEventPublisher.publishEvent(
                    new OrderCompletedEvent(
                            readUuid(payload, "orderId"),
                            readUuid(payload, "jastiperId"),
                            readNullableDouble(payload, "jastiperRating")
                    )
            );
            case STOCK_RELEASE_REQUESTED -> applicationEventPublisher.publishEvent(
                    new StockReleaseRequestedEvent(
                            event.getEventId(),
                            readUuid(payload, "productId"),
                            readInt(payload, "quantity"),
                            event.getCorrelationId()
                    )
            );
            case STOCK_RESERVATION_REQUESTED -> publishStockReservationRequested(event, payload);
            default -> throw new IllegalArgumentException("Unsupported order event type: " + eventType);
        }
    }

    private void publishStockReservationRequested(OrderOutboxEvent event, JsonNode payload) {
        UUID orderId = readUuid(payload, "orderId");
        try {
            applicationEventPublisher.publishEvent(
                    new StockReservationRequestedEvent(
                            event.getEventId(),
                            readUuid(payload, "productId"),
                            readInt(payload, "quantity"),
                            event.getCorrelationId()
                    )
            );
        } catch (RuntimeException exception) {
            applicationEventPublisher.publishEvent(
                    StockReservationFailedEvent.builder()
                            .eventId(event.getEventId())
                            .orderId(orderId)
                            .reason(exception.getMessage())
                            .build()
            );
            throw exception;
        }
    }

    private JsonNode parsePayload(String payload) {
        try {
            return OBJECT_MAPPER.readTree(payload);
        } catch (IOException exception) {
            throw new IllegalArgumentException("Invalid order event payload", exception);
        }
    }

    private UUID readUuid(JsonNode payload, String fieldName) {
        JsonNode field = requiredField(payload, fieldName);
        return UUID.fromString(field.asText());
    }

    private int readInt(JsonNode payload, String fieldName) {
        JsonNode field = requiredField(payload, fieldName);
        return field.asInt();
    }

    private Double readNullableDouble(JsonNode payload, String fieldName) {
        JsonNode field = requiredField(payload, fieldName);
        return field.isNull() ? null : field.asDouble();
    }

    private BigDecimal readBigDecimal(JsonNode payload, String fieldName) {
        JsonNode field = requiredField(payload, fieldName);
        return field.decimalValue();
    }

    private JsonNode requiredField(JsonNode payload, String fieldName) {
        JsonNode field = payload.get(fieldName);
        if (field == null || field.isMissingNode()) {
            throw new IllegalArgumentException("Missing " + fieldName);
        }
        return field;
    }
}
