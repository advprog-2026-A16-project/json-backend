package id.ac.ui.cs.advprog.jsonbackend.wallet.event;

public enum WalletOutboxEventStatus {
    PENDING,
    SENT,
    FAILED,
    DEAD_LETTER
}
