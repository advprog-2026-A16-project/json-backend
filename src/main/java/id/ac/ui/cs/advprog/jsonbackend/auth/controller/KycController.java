package id.ac.ui.cs.advprog.jsonbackend.auth.controller;

import id.ac.ui.cs.advprog.jsonbackend.auth.dto.KycRequest;
import id.ac.ui.cs.advprog.jsonbackend.auth.model.KycSubmission;
import id.ac.ui.cs.advprog.jsonbackend.auth.model.User;
import id.ac.ui.cs.advprog.jsonbackend.auth.service.KycService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/kyc")
@RequiredArgsConstructor
public class KycController {

    private final KycService kycService;

    @PostMapping("/submit")
    public ResponseEntity<String> submitKyc(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody KycRequest request) {
        kycService.submitKyc(user.getId(), request);
        return ResponseEntity.ok("KYC submitted successfully");
    }
}