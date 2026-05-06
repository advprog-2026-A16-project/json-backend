package id.ac.ui.cs.advprog.jsonbackend.profile.service;

import id.ac.ui.cs.advprog.jsonbackend.profile.dto.KycRequest;
import id.ac.ui.cs.advprog.jsonbackend.profile.model.UserProfile;

import java.util.UUID;

public interface KycService {
    UserProfile submitKyc(UUID userId, KycRequest request);
}
