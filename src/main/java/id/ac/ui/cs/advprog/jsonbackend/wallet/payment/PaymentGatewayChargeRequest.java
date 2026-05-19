package id.ac.ui.cs.advprog.jsonbackend.wallet.payment;

import java.math.BigDecimal;
import java.util.UUID;

public record PaymentGatewayChargeRequest(
        String orderId,
        BigDecimal amount,
        UUID userId,
        String description
) {
}
