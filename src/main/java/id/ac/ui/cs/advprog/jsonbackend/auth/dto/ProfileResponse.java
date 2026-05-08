package id.ac.ui.cs.advprog.jsonbackend.auth.dto;

import id.ac.ui.cs.advprog.jsonbackend.auth.enums.Role;
import id.ac.ui.cs.advprog.jsonbackend.auth.model.Profile;

import java.util.UUID;

public record ProfileResponse(
        UUID profileId,
        String email,
        String username,
        String fullName,
        String bio,
        Role role,
        boolean isVerifiedJastiper,
        Integer successfulTransactions,
        Double rating
) {
    public static ProfileResponse from(Profile profile) {
        return new ProfileResponse(
                profile.getId(),
                profile.getUser().getEmail(),
                profile.getUsername(),
                profile.getFullName(),
                profile.getBio(),
                profile.getUser().getRole(),
                profile.isVerifiedJastiper(),
                profile.getSuccessfulTransactions(),
                profile.getRating()
        );
    }
}