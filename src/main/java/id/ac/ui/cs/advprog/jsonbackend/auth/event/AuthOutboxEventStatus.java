package id.ac.ui.cs.advprog.jsonbackend.auth.event;

public enum AuthOutboxEventStatus {
    PENDING,
    SENT,
    FAILED,
    DEAD_LETTER
}