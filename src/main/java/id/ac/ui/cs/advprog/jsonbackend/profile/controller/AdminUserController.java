package id.ac.ui.cs.advprog.jsonbackend.profile.controller;

import id.ac.ui.cs.advprog.jsonbackend.profile.dto.AdminUserResponse;
import id.ac.ui.cs.advprog.jsonbackend.profile.dto.UserStatusUpdateRequest;
import id.ac.ui.cs.advprog.jsonbackend.profile.model.UserProfile;
import id.ac.ui.cs.advprog.jsonbackend.profile.service.KycService;
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

    @GetMapping("/users")
    public ResponseEntity<List<AdminUserResponse>> getAllUsers() {
        List<AdminUserResponse> responses = kycService.getAllUsers().stream()
                .map(this::convertToAdminResponse)
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
                .map(this::convertToAdminResponse)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/users/{id}/kyc/approve")
    public ResponseEntity<AdminUserResponse> approveKyc(@PathVariable UUID id) {
        UserProfile approvedProfile = kycService.approveKyc(id);
        return ResponseEntity.ok(convertToAdminResponse(approvedProfile));
    }

    @PutMapping("/users/{id}/kyc/reject")
    public ResponseEntity<AdminUserResponse> rejectKyc(@PathVariable UUID id) {
        UserProfile rejectedProfile = kycService.rejectKyc(id);
        return ResponseEntity.ok(convertToAdminResponse(rejectedProfile));
    }

    @PutMapping("/users/{id}/status")
    public ResponseEntity<AdminUserResponse> updateUserStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UserStatusUpdateRequest request) {

        UserProfile updatedProfile = kycService.updateUserStatus(id, request.accountStatus(), request.role());
        return ResponseEntity.ok(convertToAdminResponse(updatedProfile));
    }

    private AdminUserResponse convertToAdminResponse(UserProfile profile) {
        return new AdminUserResponse(
                profile.getUser().getId(),
                profile.getUser().getEmail(),
                profile.getUser().getRole(),
                profile.getUser().getAccountStatus(),
                profile.getUsername(),
                profile.getFullName(),
                profile.getKycFullName(),
                profile.getIdentityNumber(),
                profile.getSocialMediaLink(),
                profile.isVerifiedJastiper()
        );
    }
}