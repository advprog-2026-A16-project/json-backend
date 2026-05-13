package id.ac.ui.cs.advprog.jsonbackend.wallet.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import java.math.BigDecimal;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class OrderCreatedEvent {
    private UUID orderId;
    private UUID userId;
    private BigDecimal amount;
}