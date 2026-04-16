package id.ac.ui.cs.advprog.jsonbackend.profile.service;

import id.ac.ui.cs.advprog.jsonbackend.auth.model.User;
import id.ac.ui.cs.advprog.jsonbackend.profile.model.UserProfile;

import java.util.UUID;

public interface UserProfileService {
    public UserProfile createProfileForUser(User user, String requestedUsername);
    public UserProfile getProfileByUserId(UUID userId);
}
