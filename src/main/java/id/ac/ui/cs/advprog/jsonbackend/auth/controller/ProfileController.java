package id.ac.ui.cs.advprog.jsonbackend.auth.controller;

import id.ac.ui.cs.advprog.jsonbackend.auth.dto.ProfileResponse;
import id.ac.ui.cs.advprog.jsonbackend.auth.dto.UpdateProfileRequest;
import id.ac.ui.cs.advprog.jsonbackend.auth.exception.UserNotFoundException;
import id.ac.ui.cs.advprog.jsonbackend.auth.model.Profile;
import id.ac.ui.cs.advprog.jsonbackend.auth.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;

@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class ProfileController {

    private final ProfileService profileService;

    @GetMapping("/me")
    public ResponseEntity<ProfileResponse> getMyProfile(Principal principal) {
        Profile profile = profileService.getOrCreateProfileByEmail(extractCurrentEmail(principal));
        return ResponseEntity.ok(ProfileResponse.from(profile));
    }

    @PutMapping("/me")
    public ResponseEntity<ProfileResponse> updateProfile(
            Principal principal,
            @RequestBody UpdateProfileRequest request) {

        Profile updated = profileService.updateProfileByEmail(
                extractCurrentEmail(principal),
                request.username(),
                request.fullName(),
                request.bio()
        );
        return ResponseEntity.ok(ProfileResponse.from(updated));
    }

    private String extractCurrentEmail(Principal principal) {
        if (principal == null || principal.getName() == null || principal.getName().isBlank()) {
            throw new UserNotFoundException("Authenticated user not found");
        }
        return principal.getName();
    }
}
