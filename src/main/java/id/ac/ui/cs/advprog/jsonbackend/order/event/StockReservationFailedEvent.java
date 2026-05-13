package id.ac.ui.cs.advprog.jsonbackend.order.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StockReservationFailedEvent {
    private UUID eventId;
    private UUID orderId;
    private String reason;
}