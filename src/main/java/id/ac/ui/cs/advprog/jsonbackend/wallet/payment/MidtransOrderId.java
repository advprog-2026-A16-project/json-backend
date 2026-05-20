package id.ac.ui.cs.advprog.jsonbackend.wallet.payment;

import java.util.UUID;

public final class MidtransOrderId {
    private static final String WALLET_TOP_UP_PREFIX = "WALLET-TOPUP-";

    private MidtransOrderId() {
    }

    public static String forTopUp(UUID transactionId) {
        if (transactionId == null) {
            throw new IllegalArgumentException("Transaction ID is required");
        }
        return WALLET_TOP_UP_PREFIX + transactionId;
    }

    public static UUID extractTopUpTransactionId(String orderId) {
        if (orderId == null || !orderId.startsWith(WALLET_TOP_UP_PREFIX)) {
            throw new IllegalArgumentException("Unsupported Midtrans order ID");
        }
        return UUID.fromString(orderId.substring(WALLET_TOP_UP_PREFIX.length()));
    }
}
