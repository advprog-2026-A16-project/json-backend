package id.ac.ui.cs.advprog.jsonbackend.wallet.dto;

import id.ac.ui.cs.advprog.jsonbackend.wallet.model.Transaction;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PaymentGatewayTopUpResponse {
    private TransactionResponse transaction;
    private String paymentToken;
    private String paymentRedirectUrl;

    public static PaymentGatewayTopUpResponse from(Transaction transaction) {
        PaymentGatewayTopUpResponse response = new PaymentGatewayTopUpResponse();
        response.setTransaction(TransactionResponse.from(transaction));
        response.setPaymentToken(transaction.getPaymentToken());
        response.setPaymentRedirectUrl(transaction.getPaymentRedirectUrl());
        return response;
    }
}
