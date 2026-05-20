package id.ac.ui.cs.advprog.jsonbackend.wallet.event;

import java.util.UUID;

public final class WalletEventPayloadFactory {

    private WalletEventPayloadFactory() {
    }

    public static String paymentSuccessPayload(UUID orderId) {
        return String.format("{\"orderId\":\"%s\"}", orderId);
    }

    public static String paymentFailedPayload(UUID orderId, String reason) {
        return String.format(
                "{\"orderId\":\"%s\",\"reason\":\"%s\"}",
                orderId,
                escape(reason)
        );
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        return value.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
