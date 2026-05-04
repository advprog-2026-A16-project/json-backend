package id.ac.ui.cs.advprog.jsonbackend.auth.event.enums;

public enum AuthOutboxEventStatus {
    PENDING,
    SENT,
    FAILED,
    DEAD_LETTER
}