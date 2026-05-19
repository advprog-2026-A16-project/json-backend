package id.ac.ui.cs.advprog.jsonbackend.auth.dto;

import id.ac.ui.cs.advprog.jsonbackend.auth.enums.Role;
import id.ac.ui.cs.advprog.jsonbackend.auth.model.Profile;

import java.util.UUID;

public record ProfileResponse(
        UUID profileId,
        UUID userId,
        String email,
        String username,
        String fullName,
        String bio,
        Role role,
        Integer successfulTransactions,
        Double rating
) {
    public static ProfileResponse from(Profile profile) {
        return new ProfileResponse(
                profile.getId(),
                profile.getUser().getId(),
                profile.getUser().getEmail(),
                profile.getUsername(),
                profile.getFullName(),
                profile.getBio(),
                profile.getUser().getRole(),
                profile.getSuccessfulTransactions(),
                profile.getRating()
        );
    }
}
