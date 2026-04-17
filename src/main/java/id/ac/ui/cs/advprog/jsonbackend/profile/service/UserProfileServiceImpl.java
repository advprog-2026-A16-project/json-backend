package id.ac.ui.cs.advprog.jsonbackend.profile.service;

import id.ac.ui.cs.advprog.jsonbackend.auth.model.User;
import id.ac.ui.cs.advprog.jsonbackend.profile.exception.UserProfileNotFoundException;
import id.ac.ui.cs.advprog.jsonbackend.profile.model.UserProfile;
import id.ac.ui.cs.advprog.jsonbackend.profile.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserProfileServiceImpl implements UserProfileService {
    private final UserProfileRepository userProfileRepository;

    @Override
    public UserProfile createProfileForUser(User user, String requestedUsername) {
        String finalUsername = requestedUsername;

        if (finalUsername == null || finalUsername.isEmpty()) {
            finalUsername = user.getEmail().split("@")[0];
        }

        UserProfile profile = UserProfile.builder()
                .user(user)
                .username(finalUsername)
                .build();

        return userProfileRepository.save(profile);
    }

    @Override
    public UserProfile getProfileByUserId(UUID userId) {
        return userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new UserProfileNotFoundException("Profile not found"));
    }

    @Override
    public UserProfile updateProfile(UUID userId, String username, String fullName) {
        UserProfile profile = getProfileByUserId(userId);

        if (username != null && !username.trim().isEmpty()) {
            profile.setUsername(username.trim());
        }
        if (fullName != null && !fullName.trim().isEmpty()) {
            profile.setFullName(fullName.trim());
        }

        return userProfileRepository.save(profile);
    }
}