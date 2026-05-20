package id.ac.ui.cs.advprog.jsonbackend.wallet.payment;

public interface PaymentGateway {
    PaymentGatewayChargeResponse createCharge(PaymentGatewayChargeRequest request);
}
