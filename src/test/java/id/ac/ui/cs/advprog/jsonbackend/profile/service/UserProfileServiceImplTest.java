package id.ac.ui.cs.advprog.jsonbackend.profile.service;

import id.ac.ui.cs.advprog.jsonbackend.auth.model.User;
import id.ac.ui.cs.advprog.jsonbackend.profile.model.UserProfile;
import id.ac.ui.cs.advprog.jsonbackend.profile.repository.UserProfileRepository;
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
public class UserProfileServiceImplTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    @InjectMocks
    private UserProfileServiceImpl userProfileService;

    @Test
    void whenCreateProfileWithoutUsername_shouldExtractFromEmail() {
        User user = User.builder().email("leon@email.com").build();

        when(userProfileRepository.save(any())).thenAnswer(i -> i.getArguments()[0]);

        UserProfile result = userProfileService.createProfileForUser(user, null);

        assertEquals("leon", result.getUsername());
    }

    @Test
    void updateProfile_ShouldUpdateFields_WhenValidDataProvided() {
        UUID userId = UUID.randomUUID();
        UserProfile existing = UserProfile.builder().username("old").build();

        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.of(existing));
        when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(i -> i.getArguments()[0]);

        UserProfile updated = userProfileService.updateProfile(userId, "new_username", "Leon Kennedy");

        assertEquals("new_username", updated.getUsername());
        assertEquals("Joko Wito", updated.getFullName());
    }
}
