package id.ac.ui.cs.advprog.jsonbackend.auth.dto;

import id.ac.ui.cs.advprog.jsonbackend.auth.enums.Role;
import java.util.UUID;

public record UserProfileResponse(
        UUID profileId,
        String email,
        String username,
        String fullName,
        String bio,
        Role role,
        boolean isVerifiedJastiper,
        Integer successfulTransactions,
        Double rating
) {}