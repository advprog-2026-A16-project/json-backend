package id.ac.ui.cs.advprog.jsonbackend.auth.dto;


public record UpdateProfileRequest(
        String username,
        String fullName,
        String bio
) {}