package id.ac.ui.cs.advprog.jsonbackend.auth.controller;

import id.ac.ui.cs.advprog.jsonbackend.auth.dto.AdminUserResponse;
import id.ac.ui.cs.advprog.jsonbackend.auth.dto.KycSubmissionResponse;
import id.ac.ui.cs.advprog.jsonbackend.auth.dto.UserStatusUpdateRequest;
import id.ac.ui.cs.advprog.jsonbackend.auth.model.Profile;
import id.ac.ui.cs.advprog.jsonbackend.auth.service.AdminUserService;
import id.ac.ui.cs.advprog.jsonbackend.auth.service.KycService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@PreAuthorize("hasRole('ADMIN')")
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

    @PutMapping("/users/{id}/status")
    public ResponseEntity<AdminUserResponse> updateUserStatus(
            @PathVariable UUID id,
            @Valid @RequestBody UserStatusUpdateRequest request) {

        Profile updatedProfile = adminUserService.updateUserStatus(id, request.accountStatus(), request.role());
        return ResponseEntity.ok(AdminUserResponse.from(updatedProfile));
    }

    @GetMapping("/kyc/pending")
    public ResponseEntity<List<KycSubmissionResponse>> getPendingKyc() {
        List<KycSubmissionResponse> responses = kycService.getPendingKycList().stream()
                .map(KycSubmissionResponse::from)
                .toList();
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/kyc/{submissionId}/approve")
    public ResponseEntity<String> approveKyc(@PathVariable UUID submissionId) {
        kycService.approveKyc(submissionId);
        return ResponseEntity.ok("KYC approved successfully");
    }

    @PutMapping("/kyc/{submissionId}/reject")
    public ResponseEntity<String> rejectKyc(@PathVariable UUID submissionId) {
        kycService.rejectKyc(submissionId);
        return ResponseEntity.ok("KYC rejected successfully");
    }
}