package id.ac.ui.cs.advprog.jsonbackend.auth.service;

import id.ac.ui.cs.advprog.jsonbackend.auth.exception.ProfileNotFoundException;
import id.ac.ui.cs.advprog.jsonbackend.auth.model.User;
import id.ac.ui.cs.advprog.jsonbackend.auth.model.Profile;
import id.ac.ui.cs.advprog.jsonbackend.auth.repository.ProfileRepository;
import id.ac.ui.cs.advprog.jsonbackend.auth.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProfileServiceImplTest {

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ProfileServiceImpl profileService;

    @Test
    void whenCreateProfileWithoutUsername_shouldExtractFromEmail() {
        User user = User.builder().email("leon@email.com").build();

        when(profileRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        Profile result = profileService.createProfileForUser(user, null);

        assertEquals("leon", result.getUsername());
        assertEquals(0, result.getSuccessfulTransactions());
        assertEquals(0.0, result.getRating());
    }

    @Test
    void updateProfile_ShouldUpdateFields_WhenValidDataProvided() {
        UUID userId = UUID.randomUUID();
        Profile existing = Profile.builder().username("old").build();

        when(profileRepository.findByUserIdWithUser(userId)).thenReturn(Optional.of(existing));
        when(profileRepository.save(any(Profile.class))).thenAnswer(i -> i.getArguments()[0]);

        Profile updated = profileService.updateProfile(
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

        Profile result = profileService.createProfileForUser(user, "custom_username");

        assertEquals("custom_username", result.getUsername());
        assertEquals(0, result.getSuccessfulTransactions());
        assertEquals(0.0, result.getRating());
    }

    @Test
    void getProfileByUserId_ShouldReturnProfile_WhenProfileExists() {
        UUID userId = UUID.randomUUID();

        Profile profile = Profile.builder()
                .username("leon")
                .build();

        when(profileRepository.findByUserIdWithUser(userId))
                .thenReturn(Optional.of(profile));

        Profile result = profileService.getProfileByUserId(userId);

        assertEquals("leon", result.getUsername());
    }

    @Test
    void getProfileByUserId_ShouldThrowException_WhenProfileNotFound() {
        UUID userId = UUID.randomUUID();

        when(profileRepository.findByUserIdWithUser(userId))
                .thenReturn(Optional.empty());

        assertThrows(
                ProfileNotFoundException.class,
                () -> profileService.getProfileByUserId(userId)
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

        when(profileRepository.findByUserIdWithUser(userId))
                .thenReturn(Optional.of(existing));

        when(profileRepository.save(any(Profile.class)))
                .thenAnswer(i -> i.getArguments()[0]);

        Profile updated = profileService.updateProfile(
                userId,
                "   ",
                null,
                ""
        );

        assertEquals("old_username", updated.getUsername());
        assertEquals("Old Name", updated.getFullName());
        assertEquals("", updated.getBio());
    }

    @Test
    void recordSuccessfulTransaction_ShouldIncrementTransactionCount() {
        UUID userId = UUID.randomUUID();

        Profile profile = Profile.builder()
                .successfulTransactions(2)
                .rating(4.0)
                .build();

        when(profileRepository.findByUserIdWithUser(userId))
                .thenReturn(Optional.of(profile));

        when(profileRepository.save(any(Profile.class)))
                .thenAnswer(i -> i.getArguments()[0]);

        profileService.recordSuccessfulTransaction(userId, null);

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

        when(profileRepository.findByUserIdWithUser(userId))
                .thenReturn(Optional.of(profile));

        when(profileRepository.save(any(Profile.class)))
                .thenAnswer(i -> i.getArguments()[0]);

        profileService.recordSuccessfulTransaction(userId, 5.0);

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

        when(profileRepository.findByUserIdWithUser(userId))
                .thenReturn(Optional.of(profile));

        when(profileRepository.save(any(Profile.class)))
                .thenAnswer(i -> i.getArguments()[0]);

        profileService.recordSuccessfulTransaction(userId, 0.0);

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

        Profile result = profileService.createProfileForUser(user, "");

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

        when(profileRepository.findByUserIdWithUser(userId))
                .thenReturn(Optional.of(existing));

        when(profileRepository.save(any(Profile.class)))
                .thenAnswer(i -> i.getArguments()[0]);

        Profile updated = profileService.updateProfile(
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

        when(profileRepository.findByUserIdWithUser(userId))
                .thenReturn(Optional.of(existing));

        when(profileRepository.save(any(Profile.class)))
                .thenAnswer(i -> i.getArguments()[0]);

        Profile updated = profileService.updateProfile(
                userId,
                "  new_username  ",
                null,
                null
        );

        assertEquals("new_username", updated.getUsername());
    }

    @Test
    void updateProfile_ShouldUpdateAndReturnProfile_WhenProfileExists() {
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).email("test@mail.com").build();
        Profile existingProfile = Profile.builder()
                .id(UUID.randomUUID())
                .user(user)
                .username("oldUsername")
                .fullName("Old Name")
                .bio("Old Bio")
                .build();

        when(profileRepository.findByUserIdWithUser(userId)).thenReturn(Optional.of(existingProfile));
        when(profileRepository.save(any(Profile.class))).thenAnswer(i -> i.getArgument(0));

        Profile updatedProfile = profileService.updateProfile(userId, "newUsername", "New Name", "New Bio");

        assertNotNull(updatedProfile);
        assertEquals("newUsername", updatedProfile.getUsername());
        assertEquals("New Name", updatedProfile.getFullName());
        assertEquals("New Bio", updatedProfile.getBio());

        verify(profileRepository, times(1)).findByUserIdWithUser(userId);
        verify(profileRepository, times(1)).save(existingProfile);
    }

    @Test
    void updateProfile_ShouldThrowException_WhenProfileNotFound() {
        UUID userId = UUID.randomUUID();
        when(profileRepository.findByUserIdWithUser(userId)).thenReturn(Optional.empty());

        assertThrows(ProfileNotFoundException.class,
                () -> profileService.updateProfile(userId, "newUsername", "New Name", "New Bio"));

        verify(profileRepository, never()).save(any(Profile.class));
    }
}
