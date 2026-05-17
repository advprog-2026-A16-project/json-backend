package id.ac.ui.cs.advprog.jsonbackend.inventory.event;

import java.util.UUID;

public record StockReservationRequestedEvent(
        UUID eventId,
        UUID productId,
        int quantity,
        String correlationId
) {}
