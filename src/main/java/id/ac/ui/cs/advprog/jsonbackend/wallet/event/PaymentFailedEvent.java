package id.ac.ui.cs.advprog.jsonbackend.wallet.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class PaymentFailedEvent {
    private UUID orderId;
    private String reason;
}