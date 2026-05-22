package id.ac.ui.cs.advprog.jsonbackend.auth.service;

import id.ac.ui.cs.advprog.jsonbackend.auth.dto.KycRequest;
import id.ac.ui.cs.advprog.jsonbackend.auth.model.KycSubmission;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface KycService {
    KycSubmission submitKyc(UUID userId, KycRequest request);
    Optional<KycSubmission> getLatestKycSubmission(UUID userId);
    List<KycSubmission> getPendingKycList();
    void approveKyc(UUID userId);
    void rejectKyc(UUID userId);
}
