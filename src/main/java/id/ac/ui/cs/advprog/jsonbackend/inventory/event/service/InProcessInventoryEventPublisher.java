package id.ac.ui.cs.advprog.jsonbackend.inventory.event.service;

import id.ac.ui.cs.advprog.jsonbackend.inventory.event.InventoryEventType;
import id.ac.ui.cs.advprog.jsonbackend.inventory.event.NonRetryableInventoryEventException;
import id.ac.ui.cs.advprog.jsonbackend.inventory.event.StockReleaseRequestedEvent;
import id.ac.ui.cs.advprog.jsonbackend.inventory.event.StockReservationRequestedEvent;
import id.ac.ui.cs.advprog.jsonbackend.inventory.event.handler.StockReleaseRequestedEventHandler;
import id.ac.ui.cs.advprog.jsonbackend.inventory.event.handler.StockReservationRequestedEventHandler;
import id.ac.ui.cs.advprog.jsonbackend.inventory.event.model.InventoryOutboxEvent;
import org.springframework.stereotype.Component;

import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class InProcessInventoryEventPublisher implements InventoryEventPublisher {
    private static final Pattern PRODUCT_ID_PATTERN = Pattern.compile("\"productId\"\\s*:\\s*\"([^\"]+)\"");
    private static final Pattern QUANTITY_PATTERN = Pattern.compile("\"quantity\"\\s*:\\s*(\\d+)");

    private final StockReservationRequestedEventHandler stockReservationRequestedEventHandler;
    private final StockReleaseRequestedEventHandler stockReleaseRequestedEventHandler;

    public InProcessInventoryEventPublisher(
            StockReservationRequestedEventHandler stockReservationRequestedEventHandler,
            StockReleaseRequestedEventHandler stockReleaseRequestedEventHandler
    ) {
        this.stockReservationRequestedEventHandler = stockReservationRequestedEventHandler;
        this.stockReleaseRequestedEventHandler = stockReleaseRequestedEventHandler;
    }

    @Override
    public void publish(InventoryOutboxEvent event) {
        switch (event.getEventType()) {
            case STOCK_RESERVED -> {
                EventPayload payload = parsePayload(event.getPayload());
                stockReservationRequestedEventHandler.handle(
                        new StockReservationRequestedEvent(
                                event.getEventId(),
                                payload.productId(),
                                payload.quantity(),
                                event.getCorrelationId()
                        )
                );
            }
            case STOCK_RELEASED -> {
                EventPayload payload = parsePayload(event.getPayload());
                stockReleaseRequestedEventHandler.handle(
                        new StockReleaseRequestedEvent(
                                event.getEventId(),
                                payload.productId(),
                                payload.quantity(),
                                event.getCorrelationId()
                        )
                );
            }
            default -> throw new NonRetryableInventoryEventException(
                    "Unsupported inventory event type: " + event.getEventType()
            );
        }
    }

    private EventPayload parsePayload(String payload) {
        try {
            return new EventPayload(extractProductId(payload), extractQuantity(payload));
        } catch (Exception ex) {
            throw new NonRetryableInventoryEventException("Invalid inventory event payload", ex);
        }
    }

    private UUID extractProductId(String payload) {
        Matcher matcher = PRODUCT_ID_PATTERN.matcher(payload);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Missing productId");
        }
        return UUID.fromString(matcher.group(1));
    }

    private int extractQuantity(String payload) {
        Matcher matcher = QUANTITY_PATTERN.matcher(payload);
        if (!matcher.find()) {
            throw new IllegalArgumentException("Missing quantity");
        }
        int quantity = Integer.parseInt(matcher.group(1));
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be greater than zero");
        }
        return quantity;
    }

    private record EventPayload(UUID productId, int quantity) {
    }
}
