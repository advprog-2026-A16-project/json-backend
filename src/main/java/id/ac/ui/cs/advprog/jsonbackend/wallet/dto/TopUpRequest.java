package id.ac.ui.cs.advprog.jsonbackend.wallet.dto;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
public class TopUpRequest {
    private UUID userId;
    private BigDecimal amount;
}