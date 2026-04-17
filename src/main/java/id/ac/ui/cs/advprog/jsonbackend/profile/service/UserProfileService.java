package id.ac.ui.cs.advprog.jsonbackend.profile.service;

import id.ac.ui.cs.advprog.jsonbackend.auth.model.User;
import id.ac.ui.cs.advprog.jsonbackend.profile.model.UserProfile;

import java.util.UUID;

public interface UserProfileService {
    UserProfile createProfileForUser(User user, String requestedUsername);
    UserProfile getProfileByUserId(UUID userId);
    UserProfile updateProfile(UUID userId, String username, String fullName);
}
