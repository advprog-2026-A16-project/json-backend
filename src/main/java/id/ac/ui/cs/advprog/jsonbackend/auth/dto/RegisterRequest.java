package id.ac.ui.cs.advprog.jsonbackend.auth.dto;

import id.ac.ui.cs.advprog.jsonbackend.auth.model.Role;

public record RegisterRequest(String email, String password, String confirmPassword) {}