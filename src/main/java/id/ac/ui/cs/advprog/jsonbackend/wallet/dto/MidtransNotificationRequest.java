package id.ac.ui.cs.advprog.jsonbackend.wallet.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MidtransNotificationRequest {
    @JsonProperty("order_id")
    private String orderId;

    @JsonProperty("status_code")
    private String statusCode;

    @JsonProperty("gross_amount")
    private String grossAmount;

    @JsonProperty("signature_key")
    private String signatureKey;

    @JsonProperty("transaction_status")
    private String transactionStatus;

    @JsonProperty("fraud_status")
    private String fraudStatus;

    @JsonProperty("transaction_id")
    private String transactionId;

    @JsonProperty("payment_type")
    private String paymentType;
}
