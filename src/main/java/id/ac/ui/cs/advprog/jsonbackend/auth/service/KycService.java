package id.ac.ui.cs.advprog.jsonbackend.auth.service;

import id.ac.ui.cs.advprog.jsonbackend.auth.enums.AccountStatus;
import id.ac.ui.cs.advprog.jsonbackend.auth.enums.Role;
import id.ac.ui.cs.advprog.jsonbackend.auth.dto.KycRequest;
import id.ac.ui.cs.advprog.jsonbackend.auth.model.KycSubmission;
import id.ac.ui.cs.advprog.jsonbackend.auth.model.Profile;

import java.util.List;
import java.util.UUID;

public interface KycService {
    KycSubmission submitKyc(UUID userId, KycRequest request);
    List<KycSubmission> getPendingKycList();
    void approveKyc(UUID userId);
    void rejectKyc(UUID userId);
}
