package id.ac.ui.cs.advprog.jsonbackend.auth.event;

import java.util.UUID;

public record UserRegisteredEvent(
        UUID eventId,
        UUID userId,
        String email,
        String role,
        String accountStatus,
        UUID profileId,
        String username,
        double rating,
        int successfulTransactions,
        String correlationId
) {}