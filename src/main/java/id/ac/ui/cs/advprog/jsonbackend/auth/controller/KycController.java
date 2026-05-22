package id.ac.ui.cs.advprog.jsonbackend.auth.controller;

import id.ac.ui.cs.advprog.jsonbackend.auth.dto.KycRequest;
import id.ac.ui.cs.advprog.jsonbackend.auth.dto.KycSubmissionResponse;
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
    public ResponseEntity<KycSubmissionResponse> submitKyc(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody KycRequest request) {
        KycSubmission submission = kycService.submitKyc(user.getId(), request);
        return ResponseEntity.ok(KycSubmissionResponse.from(submission));
    }

    @GetMapping("/me")
    public ResponseEntity<KycSubmissionResponse> getMyLatestKyc(@AuthenticationPrincipal User user) {
        return kycService.getLatestKycSubmission(user.getId())
                .map(KycSubmissionResponse::from)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.noContent().build());
    }
}
