package id.ac.ui.cs.advprog.jsonbackend.profile.event.handler;

import id.ac.ui.cs.advprog.jsonbackend.auth.model.User;
import id.ac.ui.cs.advprog.jsonbackend.auth.repository.UserRepository;
import id.ac.ui.cs.advprog.jsonbackend.profile.event.model.UserProfileProcessedEvent;
import id.ac.ui.cs.advprog.jsonbackend.profile.event.repository.UserProfileProcessedEventRepository;
import id.ac.ui.cs.advprog.jsonbackend.profile.model.UserProfile;
import id.ac.ui.cs.advprog.jsonbackend.profile.repository.UserProfileRepository;
import id.ac.ui.cs.advprog.jsonbackend.shared.event.UserRegisteredEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRegisteredEventHandlerTest {

    @Mock
    private UserProfileRepository userProfileRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserProfileProcessedEventRepository processedEventRepository;

    @InjectMocks
    private UserRegisteredEventHandler handler;

    @Test
    void handleShouldCreateProfileAndMarkProcessedWhenEventIsNew() {
        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UserRegisteredEvent event = new UserRegisteredEvent(
                eventId, userId, "budi@email.com", "TITIPERS", "ACTIVE", "corr-1"
        );

        User mockUser = User.builder().id(userId).email("budi@email.com").build();

        when(processedEventRepository.existsByEventIdAndHandlerName(eventId, "UserRegisteredEventHandler"))
                .thenReturn(false);
        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        handler.handle(event);

        ArgumentCaptor<UserProfile> profileCaptor = ArgumentCaptor.forClass(UserProfile.class);
        verify(userProfileRepository, times(1)).save(profileCaptor.capture());
        UserProfile savedProfile = profileCaptor.getValue();

        assertEquals(mockUser, savedProfile.getUser());
        assertEquals("budi", savedProfile.getUsername());

        ArgumentCaptor<UserProfileProcessedEvent> processedCaptor = ArgumentCaptor.forClass(UserProfileProcessedEvent.class);
        verify(processedEventRepository, times(1)).save(processedCaptor.capture());
        assertEquals(eventId, processedCaptor.getValue().getEventId());
        assertEquals("UserRegisteredEventHandler", processedCaptor.getValue().getHandlerName());
    }

    @Test
    void handleShouldSkipWhenEventAlreadyProcessed() {
        UUID eventId = UUID.randomUUID();
        UserRegisteredEvent event = new UserRegisteredEvent(
                eventId, UUID.randomUUID(), "test@test.com", "TITIPERS", "ACTIVE", "corr-2"
        );

        when(processedEventRepository.existsByEventIdAndHandlerName(eventId, "UserRegisteredEventHandler"))
                .thenReturn(true);

        handler.handle(event);

        verify(userRepository, never()).findById(any());
        verify(userProfileRepository, never()).save(any());
        verify(processedEventRepository, never()).save(any());
    }

    @Test
    void handleShouldThrowExceptionWhenUserNotFound() {

        UUID eventId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        UserRegisteredEvent event = new UserRegisteredEvent(
                eventId, userId, "ghost@test.com", "TITIPERS", "ACTIVE", "corr-3"
        );

        when(processedEventRepository.existsByEventIdAndHandlerName(eventId, "UserRegisteredEventHandler"))
                .thenReturn(false);

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        IllegalStateException exception = assertThrows(IllegalStateException.class, () -> handler.handle(event));
        assertEquals("User not found for profile creation", exception.getMessage());

        verify(userProfileRepository, never()).save(any());
    }
}