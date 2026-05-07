package id.ac.ui.cs.advprog.jsonbackend.auth.service;

import id.ac.ui.cs.advprog.jsonbackend.auth.enums.AccountStatus;
import id.ac.ui.cs.advprog.jsonbackend.auth.enums.Role;
import id.ac.ui.cs.advprog.jsonbackend.auth.dto.KycRequest;
import id.ac.ui.cs.advprog.jsonbackend.auth.model.UserProfile;

import java.util.List;
import java.util.UUID;

public interface KycService {
    UserProfile submitKyc(UUID userId, KycRequest request);
    List<UserProfile> getPendingKycList();
    UserProfile approveKyc(UUID userId);
    UserProfile rejectKyc(UUID userId);
    List<UserProfile> getAllUsers();
    UserProfile updateUserStatus(UUID userId, AccountStatus status, Role role);
}
