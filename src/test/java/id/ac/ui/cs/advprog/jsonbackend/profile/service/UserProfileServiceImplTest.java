package id.ac.ui.cs.advprog.jsonbackend.profile.service;

import id.ac.ui.cs.advprog.jsonbackend.auth.model.User;
import id.ac.ui.cs.advprog.jsonbackend.profile.model.UserProfile;
import id.ac.ui.cs.advprog.jsonbackend.profile.repository.UserProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

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

        UserProfile result = UserProfileService.createProfileForUser(user, null);

        assertEquals("leon", result.getUsername());
    }
}
