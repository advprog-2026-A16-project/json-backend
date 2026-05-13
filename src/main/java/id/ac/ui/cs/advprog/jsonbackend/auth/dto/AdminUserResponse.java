package id.ac.ui.cs.advprog.jsonbackend.auth.dto;

import id.ac.ui.cs.advprog.jsonbackend.auth.enums.AccountStatus;
import id.ac.ui.cs.advprog.jsonbackend.auth.enums.Role;
import id.ac.ui.cs.advprog.jsonbackend.auth.model.Profile;

import java.util.UUID;

public record AdminUserResponse(
        UUID userId,
        String email,
        Role role,
        AccountStatus accountStatus,
        String username,
        String fullName
) {
    public static AdminUserResponse from(Profile profile) {
        return new AdminUserResponse(
                profile.getUser().getId(),
                profile.getUser().getEmail(),
                profile.getUser().getRole(),
                profile.getUser().getAccountStatus(),
                profile.getUsername(),
                profile.getFullName()
        );
    }
}