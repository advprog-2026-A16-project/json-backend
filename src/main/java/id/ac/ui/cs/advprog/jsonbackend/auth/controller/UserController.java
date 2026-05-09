package id.ac.ui.cs.advprog.jsonbackend.auth.controller;

import id.ac.ui.cs.advprog.jsonbackend.auth.dto.PublicProfileResponse;
import id.ac.ui.cs.advprog.jsonbackend.auth.model.Profile;
import id.ac.ui.cs.advprog.jsonbackend.auth.service.ProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final ProfileService profileService;

    @GetMapping("/{id}")
    public ResponseEntity<PublicProfileResponse> getPublicProfile(@PathVariable UUID id) {
        Profile profile = profileService.getProfileByUserId(id);
        return ResponseEntity.ok(PublicProfileResponse.from(profile));
    }
}