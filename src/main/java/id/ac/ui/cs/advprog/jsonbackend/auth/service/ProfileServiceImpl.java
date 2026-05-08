package id.ac.ui.cs.advprog.jsonbackend.auth.service;

import id.ac.ui.cs.advprog.jsonbackend.auth.model.User;
import id.ac.ui.cs.advprog.jsonbackend.auth.exception.ProfileNotFoundException;
import id.ac.ui.cs.advprog.jsonbackend.auth.model.Profile;
import id.ac.ui.cs.advprog.jsonbackend.auth.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final ProfileRepository profileRepository;

    @Override
    public Profile createProfileForUser(User user, String requestedUsername) {
        String finalUsername = requestedUsername;

        if (finalUsername == null || finalUsername.isEmpty()) {
            finalUsername = user.getEmail().split("@")[0];
        }

        Profile profile = Profile.builder()
                .user(user)
                .username(finalUsername)
                .build();

        return profileRepository.save(profile);
    }

    @Override
    public Profile getProfileByUserId(UUID userId) {
        return profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ProfileNotFoundException("Profile not found"));
    }

    @Override
    public Profile updateProfile(UUID userId, String username, String fullName, String bio) {
        Profile profile = getProfileByUserId(userId);

        if (username != null && !username.trim().isEmpty()) {
            profile.setUsername(username.trim());
        }
        if (fullName != null && !fullName.trim().isEmpty()) {
            profile.setFullName(fullName.trim());
        }
        if (bio != null && !bio.trim().isEmpty()) {
            profile.setBio(bio.trim());
        }

        return profileRepository.save(profile);
    }

    @Override
    @Transactional
    public void recordSuccessfulTransaction(UUID userId, Double rating) {
        Profile profile = getProfileByUserId(userId);

        int currentTransactions = profile.getSuccessfulTransactions();
        profile.setSuccessfulTransactions(currentTransactions + 1);

        if (rating != null && rating > 0) {
            double currentRating = profile.getRating();
            int newTransactionCount = profile.getSuccessfulTransactions();

            double newRating = ((currentRating * currentTransactions) + rating) / newTransactionCount;
            profile.setRating(newRating);
        }

        profileRepository.save(profile);
    }
}