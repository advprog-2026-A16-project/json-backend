package id.ac.ui.cs.advprog.jsonbackend.wallet.payment;

import id.ac.ui.cs.advprog.jsonbackend.wallet.dto.MidtransNotificationRequest;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MidtransNotificationVerifierTest {

    @Test
    void isValidShouldAcceptMatchingSignature() {
        MidtransNotificationVerifier verifier = new MidtransNotificationVerifier("server-key");
        MidtransNotificationRequest notification = notification();
        notification.setSignatureKey(verifier.calculateSignature(
                notification.getOrderId(),
                notification.getStatusCode(),
                notification.getGrossAmount()
        ));

        assertTrue(verifier.isValid(notification));
    }

    @Test
    void isValidShouldRejectInvalidSignature() {
        MidtransNotificationVerifier verifier = new MidtransNotificationVerifier("server-key");
        MidtransNotificationRequest notification = notification();
        notification.setSignatureKey("invalid");

        assertFalse(verifier.isValid(notification));
    }

    private MidtransNotificationRequest notification() {
        MidtransNotificationRequest notification = new MidtransNotificationRequest();
        notification.setOrderId("WALLET-TOPUP-11111111-1111-1111-1111-111111111111");
        notification.setStatusCode("200");
        notification.setGrossAmount("50000.00");
        notification.setTransactionStatus("settlement");
        return notification;
    }
}
