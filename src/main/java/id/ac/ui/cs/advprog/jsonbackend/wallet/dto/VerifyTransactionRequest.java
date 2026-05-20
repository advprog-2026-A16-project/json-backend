package id.ac.ui.cs.advprog.jsonbackend.wallet.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class VerifyTransactionRequest {
    @NotNull(message = "Verification result is required")
    private Boolean success;

    private String description;
}
