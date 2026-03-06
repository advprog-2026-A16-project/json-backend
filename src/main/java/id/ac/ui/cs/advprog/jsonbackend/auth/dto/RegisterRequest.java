package id.ac.ui.cs.advprog.jsonbackend.auth.dto;

public record RegisterRequest(String email, String password, String confirmPassword) {}