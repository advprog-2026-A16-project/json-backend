package id.ac.ui.cs.advprog.jsonbackend.profile.dto;

import id.ac.ui.cs.advprog.jsonbackend.auth.enums.Role;
import java.util.UUID;

public record UserProfileResponse(
        UUID profileId,
        String email,
        String username,
        String fullName,
        Role role,
        Integer successfulTransactions,
        Double rating
) {}