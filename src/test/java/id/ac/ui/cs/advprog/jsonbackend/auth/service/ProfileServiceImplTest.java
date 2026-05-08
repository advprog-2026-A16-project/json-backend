package id.ac.ui.cs.advprog.jsonbackend.auth.service;

import id.ac.ui.cs.advprog.jsonbackend.auth.exception.ProfileNotFoundException;
import id.ac.ui.cs.advprog.jsonbackend.auth.model.User;
import id.ac.ui.cs.advprog.jsonbackend.auth.model.Profile;
import id.ac.ui.cs.advprog.jsonbackend.auth.repository.ProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class ProfileServiceImplTest {

    @Mock
    private ProfileRepository profileRepository;

    @InjectMocks
    private ProfileServiceImpl userProfileService;

    @Test
    void whenCreateProfileWithoutUsername_shouldExtractFromEmail() {
        User user = User.builder().email("leon@email.com").build();

        when(profileRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        Profile result = userProfileService.createProfileForUser(user, null);

        assertEquals("leon", result.getUsername());
    }

    @Test
    void updateProfile_ShouldUpdateFields_WhenValidDataProvided() {
        UUID userId = UUID.randomUUID();
        Profile existing = Profile.builder().username("old").build();

        when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(existing));
        when(profileRepository.save(any(Profile.class))).thenAnswer(i -> i.getArguments()[0]);

        Profile updated = userProfileService.updateProfile(
                userId,
                "new_username",
                "Leon Kennedy",
                "ini bio"
        );

        assertEquals("new_username", updated.getUsername());
        assertEquals("Leon Kennedy", updated.getFullName());
    }

    @Test
    void whenCreateProfileWithUsername_shouldUseRequestedUsername() {
        User user = User.builder().email("leon@email.com").build();

        when(profileRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        Profile result = userProfileService.createProfileForUser(user, "custom_username");

        assertEquals("custom_username", result.getUsername());
    }

    @Test
    void getProfileByUserId_ShouldReturnProfile_WhenProfileExists() {
        UUID userId = UUID.randomUUID();

        Profile profile = Profile.builder()
                .username("leon")
                .build();

        when(profileRepository.findByUserId(userId))
                .thenReturn(Optional.of(profile));

        Profile result = userProfileService.getProfileByUserId(userId);

        assertEquals("leon", result.getUsername());
    }

    @Test
    void getProfileByUserId_ShouldThrowException_WhenProfileNotFound() {
        UUID userId = UUID.randomUUID();

        when(profileRepository.findByUserId(userId))
                .thenReturn(Optional.empty());

        assertThrows(
                ProfileNotFoundException.class,
                () -> userProfileService.getProfileByUserId(userId)
        );
    }

    @Test
    void updateProfile_ShouldIgnoreBlankFields() {
        UUID userId = UUID.randomUUID();

        Profile existing = Profile.builder()
                .username("old_username")
                .fullName("Old Name")
                .bio("Old Bio")
                .build();

        when(profileRepository.findByUserId(userId))
                .thenReturn(Optional.of(existing));

        when(profileRepository.save(any(Profile.class)))
                .thenAnswer(i -> i.getArguments()[0]);

        Profile updated = userProfileService.updateProfile(
                userId,
                "   ",
                null,
                ""
        );

        assertEquals("old_username", updated.getUsername());
        assertEquals("Old Name", updated.getFullName());
        assertEquals("Old Bio", updated.getBio());
    }

    @Test
    void recordSuccessfulTransaction_ShouldIncrementTransactionCount() {
        UUID userId = UUID.randomUUID();

        Profile profile = Profile.builder()
                .successfulTransactions(2)
                .rating(4.0)
                .build();

        when(profileRepository.findByUserId(userId))
                .thenReturn(Optional.of(profile));

        when(profileRepository.save(any(Profile.class)))
                .thenAnswer(i -> i.getArguments()[0]);

        userProfileService.recordSuccessfulTransaction(userId, null);

        assertEquals(3, profile.getSuccessfulTransactions());
        assertEquals(4.0, profile.getRating());
    }

    @Test
    void recordSuccessfulTransaction_ShouldUpdateRating_WhenRatingProvided() {
        UUID userId = UUID.randomUUID();

        Profile profile = Profile.builder()
                .successfulTransactions(2)
                .rating(4.0)
                .build();

        when(profileRepository.findByUserId(userId))
                .thenReturn(Optional.of(profile));

        when(profileRepository.save(any(Profile.class)))
                .thenAnswer(i -> i.getArguments()[0]);

        userProfileService.recordSuccessfulTransaction(userId, 5.0);

        assertEquals(3, profile.getSuccessfulTransactions());
        assertEquals(4.333333333333333, profile.getRating());
    }

    @Test
    void recordSuccessfulTransaction_ShouldNotUpdateRating_WhenRatingIsZeroOrNegative() {
        UUID userId = UUID.randomUUID();

        Profile profile = Profile.builder()
                .successfulTransactions(1)
                .rating(5.0)
                .build();

        when(profileRepository.findByUserId(userId))
                .thenReturn(Optional.of(profile));

        when(profileRepository.save(any(Profile.class)))
                .thenAnswer(i -> i.getArguments()[0]);

        userProfileService.recordSuccessfulTransaction(userId, 0.0);

        assertEquals(2, profile.getSuccessfulTransactions());
        assertEquals(5.0, profile.getRating());
    }

    @Test
    void whenCreateProfileWithEmptyUsername_shouldExtractFromEmail() {
        User user = User.builder()
                .email("leon@email.com")
                .build();

        when(profileRepository.save(any()))
                .thenAnswer(i -> i.getArguments()[0]);

        Profile result = userProfileService.createProfileForUser(user, "");

        assertEquals("leon", result.getUsername());
    }

    @Test
    void updateProfile_ShouldIgnoreNullFields() {
        UUID userId = UUID.randomUUID();

        Profile existing = Profile.builder()
                .username("old_username")
                .fullName("Old Name")
                .bio("Old Bio")
                .build();

        when(profileRepository.findByUserId(userId))
                .thenReturn(Optional.of(existing));

        when(profileRepository.save(any(Profile.class)))
                .thenAnswer(i -> i.getArguments()[0]);

        Profile updated = userProfileService.updateProfile(
                userId,
                null,
                null,
                null
        );

        assertEquals("old_username", updated.getUsername());
        assertEquals("Old Name", updated.getFullName());
        assertEquals("Old Bio", updated.getBio());
    }

    @Test
    void updateProfile_ShouldTrimUsername() {
        UUID userId = UUID.randomUUID();

        Profile existing = Profile.builder()
                .username("old")
                .build();

        when(profileRepository.findByUserId(userId))
                .thenReturn(Optional.of(existing));

        when(profileRepository.save(any(Profile.class)))
                .thenAnswer(i -> i.getArguments()[0]);

        Profile updated = userProfileService.updateProfile(
                userId,
                "  new_username  ",
                null,
                null
        );

        assertEquals("new_username", updated.getUsername());
    }
}
