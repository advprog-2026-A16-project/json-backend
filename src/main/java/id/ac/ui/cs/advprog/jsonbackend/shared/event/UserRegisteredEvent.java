package id.ac.ui.cs.advprog.jsonbackend.shared.event;

import java.util.UUID;

public record UserRegisteredEvent(
        UUID eventId,
        UUID userId,
        String email,
        String correlationId
) {}