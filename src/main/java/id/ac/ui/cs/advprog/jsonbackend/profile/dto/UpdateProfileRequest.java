package id.ac.ui.cs.advprog.jsonbackend.profile.dto;


public record UpdateProfileRequest(
        String username,
        String fullName
) {}