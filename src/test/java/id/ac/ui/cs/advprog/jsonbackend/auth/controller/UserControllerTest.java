package id.ac.ui.cs.advprog.jsonbackend.auth.controller;

import id.ac.ui.cs.advprog.jsonbackend.auth.dto.PublicProfileResponse;
import id.ac.ui.cs.advprog.jsonbackend.auth.enums.Role;
import id.ac.ui.cs.advprog.jsonbackend.auth.exception.ProfileNotFoundException;
import id.ac.ui.cs.advprog.jsonbackend.auth.model.Profile;
import id.ac.ui.cs.advprog.jsonbackend.auth.model.User;
import id.ac.ui.cs.advprog.jsonbackend.auth.service.ProfileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserControllerTest {

    @Mock
    private ProfileService profileService;

    @InjectMocks
    private UserController userController;

    private UUID targetUserId;
    private Profile mockProfile;

    @BeforeEach
    void setUp() {
        targetUserId = UUID.randomUUID();
        User mockUser = new User();
        mockUser.setId(targetUserId);
        mockUser.setEmail("public@email.com");
        mockUser.setRole(Role.JASTIPER);

        mockProfile = Profile.builder()
                .id(UUID.randomUUID())
                .user(mockUser)
                .username("public_user")
                .fullName("Public User")
                .bio("Public Bio")
                .successfulTransactions(15)
                .rating(4.8)
                .build();
    }

    @Test
    void getPublicProfile_ShouldReturnProfileResponse_WhenUserExists() {
        when(profileService.getProfileByUserId(targetUserId)).thenReturn(mockProfile);

        ResponseEntity<PublicProfileResponse> response = userController.getPublicProfile(targetUserId);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("public_user", response.getBody().username());
        assertEquals("Public User", response.getBody().fullName());
        verify(profileService, times(1)).getProfileByUserId(targetUserId);
    }

    @Test
    void getPublicProfile_ShouldThrowException_WhenUserNotFound() {
        when(profileService.getProfileByUserId(targetUserId))
                .thenThrow(new ProfileNotFoundException("Profile tidak ditemukan"));

        assertThrows(ProfileNotFoundException.class,
                () -> userController.getPublicProfile(targetUserId));

        verify(profileService, times(1)).getProfileByUserId(targetUserId);
    }
}