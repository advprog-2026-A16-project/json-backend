package id.ac.ui.cs.advprog.jsonbackend.auth.service;

import id.ac.ui.cs.advprog.jsonbackend.auth.model.User;
import id.ac.ui.cs.advprog.jsonbackend.auth.model.Profile;

import java.util.UUID;

public interface ProfileService {
    Profile createProfileForUser(User user, String requestedUsername);
    Profile getProfileByUserId(UUID userId);
    Profile updateProfile(UUID userId, String username, String fullName, String bio);
}
