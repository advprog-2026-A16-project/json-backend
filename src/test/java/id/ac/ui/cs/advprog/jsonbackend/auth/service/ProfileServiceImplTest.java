package id.ac.ui.cs.advprog.jsonbackend.auth.service;

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
}
