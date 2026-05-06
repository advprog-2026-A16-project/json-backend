package id.ac.ui.cs.advprog.jsonbackend.profile.dto;

import id.ac.ui.cs.advprog.jsonbackend.auth.enums.AccountStatus;
import id.ac.ui.cs.advprog.jsonbackend.auth.enums.Role;
import jakarta.validation.constraints.NotNull;

public record UserStatusUpdateRequest(
        @NotNull(message = "Status akun tidak boleh kosong") AccountStatus accountStatus,
        @NotNull(message = "Role akun tidak boleh kosong") Role role
) {}