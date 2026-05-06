package id.ac.ui.cs.advprog.jsonbackend.profile.controller;

import id.ac.ui.cs.advprog.jsonbackend.auth.model.User;
import id.ac.ui.cs.advprog.jsonbackend.profile.dto.KycRequest;
import id.ac.ui.cs.advprog.jsonbackend.profile.dto.UpdateProfileRequest;
import id.ac.ui.cs.advprog.jsonbackend.profile.dto.UserProfileResponse;
import id.ac.ui.cs.advprog.jsonbackend.profile.model.UserProfile;
import id.ac.ui.cs.advprog.jsonbackend.profile.service.KycService;
import id.ac.ui.cs.advprog.jsonbackend.profile.service.UserProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService userProfileService;
    private final KycService kycService;

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMyProfile(@AuthenticationPrincipal User user) {
        UserProfile profile = userProfileService.getProfileByUserId(user.getId());
        return ResponseEntity.ok(convertToResponse(profile));
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> updateProfile(
            @AuthenticationPrincipal User user,
            @RequestBody UpdateProfileRequest request) {

        UserProfile updated = userProfileService.updateProfile(
                user.getId(),
                request.username(),
                request.fullName(),
                request.bio()
        );
        return ResponseEntity.ok(convertToResponse(updated));
    }

    private UserProfileResponse convertToResponse(UserProfile profile) {
        return new UserProfileResponse(
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
    public ResponseEntity<UserProfileResponse> submitKyc(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody KycRequest request) {
        UserProfile updated = kycService.submitKyc(user.getId(), request);
        return ResponseEntity.ok(convertToResponse(updated));
    }
}