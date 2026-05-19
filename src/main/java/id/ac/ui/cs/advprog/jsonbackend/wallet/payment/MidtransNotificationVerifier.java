package id.ac.ui.cs.advprog.jsonbackend.wallet.payment;

import id.ac.ui.cs.advprog.jsonbackend.wallet.dto.MidtransNotificationRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Component
public class MidtransNotificationVerifier {
    private final String serverKey;

    public MidtransNotificationVerifier(@Value("${midtrans.server-key:}") String serverKey) {
        this.serverKey = serverKey;
    }

    public boolean isValid(MidtransNotificationRequest notification) {
        if (notification == null || isBlank(serverKey)
                || isBlank(notification.getOrderId())
                || isBlank(notification.getStatusCode())
                || isBlank(notification.getGrossAmount())
                || isBlank(notification.getSignatureKey())) {
            return false;
        }

        String expected = calculateSignature(
                notification.getOrderId(),
                notification.getStatusCode(),
                notification.getGrossAmount()
        );
        return MessageDigest.isEqual(
                expected.toLowerCase().getBytes(StandardCharsets.UTF_8),
                notification.getSignatureKey().toLowerCase().getBytes(StandardCharsets.UTF_8)
        );
    }

    public String calculateSignature(String orderId, String statusCode, String grossAmount) {
        return sha512(orderId + statusCode + grossAmount + serverKey);
    }

    private String sha512(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-512");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder(hash.length * 2);
            for (byte b : hash) {
                hex.append(Character.forDigit((b >> 4) & 0xF, 16));
                hex.append(Character.forDigit(b & 0xF, 16));
            }
            return hex.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-512 is not available", e);
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }
}
