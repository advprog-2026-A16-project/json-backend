package id.ac.ui.cs.advprog.jsonbackend.wallet.dto;

import java.math.BigDecimal;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
public class WalletResponse {
    private UUID userId;
    private BigDecimal balance;

    public WalletResponse(UUID userId, BigDecimal balance) {
        this.userId = userId;
        this.balance = balance;
    }
}
