package id.ac.ui.cs.advprog.jsonbackend.inventory.event.service;

import id.ac.ui.cs.advprog.jsonbackend.inventory.event.InventoryEventType;
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
        try {
            UUID productId = extractProductId(event.getPayload());
            int quantity = extractQuantity(event.getPayload());

            if (event.getEventType() == InventoryEventType.STOCK_RESERVED) {
                stockReservationRequestedEventHandler.handle(
                        new StockReservationRequestedEvent(
                                event.getEventId(),
                                productId,
                                quantity,
                                event.getCorrelationId()
                        )
                );
                return;
            }

            if (event.getEventType() == InventoryEventType.STOCK_RELEASED) {
                stockReleaseRequestedEventHandler.handle(
                        new StockReleaseRequestedEvent(
                                event.getEventId(),
                                productId,
                                quantity,
                                event.getCorrelationId()
                        )
                );
            }
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid inventory event payload", ex);
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
        return Integer.parseInt(matcher.group(1));
    }
}
