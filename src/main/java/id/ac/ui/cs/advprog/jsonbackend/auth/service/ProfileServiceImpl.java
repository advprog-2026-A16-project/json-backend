package id.ac.ui.cs.advprog.jsonbackend.auth.service;

import id.ac.ui.cs.advprog.jsonbackend.auth.exception.UsernameAlreadyExistsException;
import id.ac.ui.cs.advprog.jsonbackend.auth.model.User;
import id.ac.ui.cs.advprog.jsonbackend.auth.exception.ProfileNotFoundException;
import id.ac.ui.cs.advprog.jsonbackend.auth.exception.UserNotFoundException;
import id.ac.ui.cs.advprog.jsonbackend.auth.model.Profile;
import id.ac.ui.cs.advprog.jsonbackend.auth.repository.ProfileRepository;
import id.ac.ui.cs.advprog.jsonbackend.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProfileServiceImpl implements ProfileService {

    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;

    @Override
    public Profile createProfileForUser(User user, String requestedUsername) {
        String finalUsername = requestedUsername;

        if (finalUsername == null || finalUsername.isEmpty()) {
            finalUsername = user.getEmail().split("@")[0];
        }

        String baseUsername = finalUsername;
        int counter = 1;
        while (profileRepository.existsByUsername(finalUsername)) {
            finalUsername = baseUsername + counter;
            counter++;
        }

        Profile profile = Profile.builder()
                .user(user)
                .username(finalUsername)
                .build();

        return profileRepository.save(profile);
    }

    @Override
    public Profile getProfileByUserId(UUID userId) {
        return profileRepository.findByUserIdWithUser(userId)
                .orElseThrow(() -> new ProfileNotFoundException("Profile not found"));
    }

    @Override
    @Transactional
    public Profile getOrCreateProfileByEmail(String email) {
        if (email == null || email.isBlank()) {
            throw new UserNotFoundException("Authenticated user not found");
        }

        return profileRepository.findByUserEmailWithUser(email)
                .orElseGet(() -> {
                    User user = userRepository.findByEmail(email)
                            .orElseThrow(() -> new UserNotFoundException("User not found"));
                    String generatedUsername = user.getEmail().split("@")[0];
                    return createProfileForUser(user, generatedUsername);
                });
    }

    @Override
    public Profile updateProfile(UUID userId, String username, String fullName, String bio) {
        Profile profile = getProfileByUserId(userId);

        if (username != null && !username.trim().isEmpty()) {
            String newUsername = username.trim();

            if (!newUsername.equals(profile.getUsername()) && profileRepository.existsByUsername(newUsername)) {
                throw new UsernameAlreadyExistsException("Username is already taken");
            }

            profile.setUsername(newUsername);
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
    public Profile updateProfileByEmail(String email, String username, String fullName, String bio) {
        Profile profile = getOrCreateProfileByEmail(email);
        return updateProfile(profile.getUser().getId(), username, fullName, bio);
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
