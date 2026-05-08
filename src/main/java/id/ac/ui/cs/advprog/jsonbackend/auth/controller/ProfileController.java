package id.ac.ui.cs.advprog.jsonbackend.auth.controller;

import id.ac.ui.cs.advprog.jsonbackend.auth.model.User;
import id.ac.ui.cs.advprog.jsonbackend.auth.dto.KycRequest;
import id.ac.ui.cs.advprog.jsonbackend.auth.dto.UpdateProfileRequest;
import id.ac.ui.cs.advprog.jsonbackend.auth.dto.ProfileResponse;
import id.ac.ui.cs.advprog.jsonbackend.auth.model.Profile;
import id.ac.ui.cs.advprog.jsonbackend.auth.service.KycService;
import id.ac.ui.cs.advprog.jsonbackend.auth.service.ProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;
    private final KycService kycService;

    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getMyProfile(@AuthenticationPrincipal User user) {
        Profile profile = profileService.getProfileByUserId(user.getId());
        return ResponseEntity.ok(convertToResponse(profile));
    }

    @PutMapping("/me")
    public ResponseEntity<ProfileResponse> updateProfile(
            @AuthenticationPrincipal User user,
            @RequestBody UpdateProfileRequest request) {

        Profile updated = profileService.updateProfile(
                user.getId(),
                request.username(),
                request.fullName(),
                request.bio()
        );
        return ResponseEntity.ok(convertToResponse(updated));
    }

    private ProfileResponse convertToResponse(Profile profile) {
        return new ProfileResponse(
                profile.getId(),
                profile.getUser().getEmail(),
                profile.getUsername(),
                profile.getFullName(),
                profile.getBio(),
                profile.getUser().getRole(),
                profile.isVerifiedJastiper(),
                profile.getSuccessfulTransactions(),
                profile.getRating()
        );
    }

    @PostMapping("/kyc")
    public ResponseEntity<ProfileResponse> submitKyc(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody KycRequest request) {
        Profile updated = kycService.submitKyc(user.getId(), request);
        return ResponseEntity.ok(convertToResponse(updated));
    }
}