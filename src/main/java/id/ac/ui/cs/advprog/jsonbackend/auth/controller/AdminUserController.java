package id.ac.ui.cs.advprog.jsonbackend.auth.controller;

import id.ac.ui.cs.advprog.jsonbackend.auth.dto.AdminUserResponse;
import id.ac.ui.cs.advprog.jsonbackend.auth.dto.UserStatusUpdateRequest;
import id.ac.ui.cs.advprog.jsonbackend.auth.model.Profile;
import id.ac.ui.cs.advprog.jsonbackend.auth.service.AdminUserService;
import id.ac.ui.cs.advprog.jsonbackend.auth.service.KycService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
public class AdminUserController {

    private final KycService kycService;
    private final AdminUserService adminUserService;

    @GetMapping("/users")
    public ResponseEntity<List<AdminUserResponse>> getAllUsers() {
        List<AdminUserResponse> responses = adminUserService.getAllUsers().stream()
                .map(AdminUserResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/kyc")
    public ResponseEntity<List<AdminUserResponse>> getPendingKyc(
            @RequestParam(name = "status", required = false, defaultValue = "PENDING") String status) {

        if (!"PENDING".equalsIgnoreCase(status)) {
            return ResponseEntity.badRequest().build();
        }

        List<AdminUserResponse> responses = kycService.getPendingKycList().stream()
                .map(AdminUserResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/users/{id}/kyc/approve")
    public ResponseEntity<AdminUserResponse> approveKyc(@PathVariable UUID id) {
        Profile approvedProfile = kycService.approveKyc(id);
        return ResponseEntity.ok(AdminUserResponse.from(approvedProfile));
    }

    @PutMapping("/users/{id}/kyc/reject")
    public ResponseEntity<AdminUserResponse> rejectKyc(@PathVariable UUID id) {
        Profile rejectedProfile = kycService.rejectKyc(id);
        return ResponseEntity.ok(AdminUserResponse.from(rejectedProfile));
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<AdminUserResponse> updateUserStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UserStatusUpdateRequest request) {

        Profile updatedProfile = adminUserService.updateUserStatus(id, request.accountStatus(), request.role());
        return ResponseEntity.ok(AdminUserResponse.from(updatedProfile));
    }
}