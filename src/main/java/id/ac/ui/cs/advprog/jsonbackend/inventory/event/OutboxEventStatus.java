package id.ac.ui.cs.advprog.jsonbackend.inventory.event;

public enum OutboxEventStatus {
    PENDING,
    SENT,
    FAILED,
    DEAD_LETTER
}
