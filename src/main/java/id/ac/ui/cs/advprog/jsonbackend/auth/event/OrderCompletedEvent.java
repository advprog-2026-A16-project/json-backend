package id.ac.ui.cs.advprog.jsonbackend.auth.event;

import java.util.UUID;

public record OrderCompletedEvent(
        UUID orderId,
        UUID jastiperId,
        Double rating
) {}