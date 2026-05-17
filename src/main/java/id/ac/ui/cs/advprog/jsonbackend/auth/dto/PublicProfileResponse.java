package id.ac.ui.cs.advprog.jsonbackend.auth.dto;

import id.ac.ui.cs.advprog.jsonbackend.auth.enums.Role;
import id.ac.ui.cs.advprog.jsonbackend.auth.model.Profile;

import java.util.UUID;

public record PublicProfileResponse(
        UUID userId,
        String username,
        String fullName,
        String bio,
        Role role,
        Integer successfulTransactions,
        Double rating
) {
    public static PublicProfileResponse from(Profile profile) {
        return new PublicProfileResponse(
                profile.getUser().getId(),
                profile.getUsername(),
                profile.getFullName(),
                profile.getBio(),
                profile.getUser().getRole(),
                profile.getSuccessfulTransactions(),
                profile.getRating()
        );
    }
}