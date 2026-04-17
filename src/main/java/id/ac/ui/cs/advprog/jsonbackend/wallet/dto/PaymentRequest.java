package id.ac.ui.cs.advprog.jsonbackend.wallet.dto;

import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
public class PaymentRequest {
    private UUID userId;
    private BigDecimal amount;
}
