package id.ac.ui.cs.advprog.jsonbackend.profile.listener;

import id.ac.ui.cs.advprog.jsonbackend.auth.model.User;
import id.ac.ui.cs.advprog.jsonbackend.profile.model.UserProfile;
import id.ac.ui.cs.advprog.jsonbackend.profile.repository.UserProfileRepository;
import id.ac.ui.cs.advprog.jsonbackend.shared.event.UserRegisteredEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class UserProfileEventListenerTest {

    @Mock
    private UserProfileRepository userProfileRepository;

    @InjectMocks
    private UserProfileEventListener eventListener;

    @Test
    void whenUserRegisteredEventIsFired_shouldCreateProfileWithExtractedUsername() {
        User mockUser = User.builder().email("leon@gmail.com").build();
        UserRegisteredEvent event = new UserRegisteredEvent(mockUser);

        eventListener.handleUserRegisteredEvent(event);

        ArgumentCaptor<UserProfile> profileCaptor = ArgumentCaptor.forClass(UserProfile.class);
        verify(userProfileRepository).save(profileCaptor.capture());

        UserProfile savedProfile = profileCaptor.getValue();
        assertEquals(mockUser, savedProfile.getUser());
        assertEquals("leon", savedProfile.getUsername());
    }
}