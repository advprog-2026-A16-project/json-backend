package id.ac.ui.cs.advprog.jsonbackend.order.event;

public enum OutboxEventStatus {
    PENDING,
    PROCESSED,
    FAILED
}