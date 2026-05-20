package id.ac.ui.cs.advprog.jsonbackend.auth.dto;

import id.ac.ui.cs.advprog.jsonbackend.auth.enums.Role;

import java.util.UUID;

public record AuthResponse(
        String token,
        String message,
        String email,
        Role role,
        UUID userId
) {}
