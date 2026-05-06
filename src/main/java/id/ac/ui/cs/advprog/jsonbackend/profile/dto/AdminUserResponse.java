package id.ac.ui.cs.advprog.jsonbackend.profile.dto;

import id.ac.ui.cs.advprog.jsonbackend.auth.enums.AccountStatus;
import id.ac.ui.cs.advprog.jsonbackend.auth.enums.Role;

import java.util.UUID;

public record AdminUserResponse(
        UUID userId,
        String email,
        Role role,
        AccountStatus accountStatus,
        String username,
        String fullName,
        String kycFullName,
        String identityNumber,
        String socialMediaLink,
        boolean isVerifiedJastiper
) {}