package id.ac.ui.cs.advprog.jsonbackend.auth.controller;

import id.ac.ui.cs.advprog.jsonbackend.auth.dto.ProfileResponse;
import id.ac.ui.cs.advprog.jsonbackend.auth.dto.UpdateProfileRequest;
import id.ac.ui.cs.advprog.jsonbackend.auth.enums.Role;
import id.ac.ui.cs.advprog.jsonbackend.auth.exception.GlobalExceptionHandler;
import id.ac.ui.cs.advprog.jsonbackend.auth.exception.ProfileNotFoundException;
import id.ac.ui.cs.advprog.jsonbackend.auth.exception.UserNotFoundException;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.security.Principal;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
class ProfileControllerTest {

    @Mock
    private ProfileService profileService;

    @InjectMocks
    private ProfileController profileController;

    private MockMvc mockMvc;

    private User mockUser;
    private UUID userId;
    private Principal principal;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(profileController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();

        userId = UUID.randomUUID();

        mockUser = new User();
        mockUser.setId(userId);
        mockUser.setEmail("test@email.com");
        mockUser.setRole(Role.TITIPERS);
        principal = () -> "test@email.com";
    }

    @Test
    @WithMockUser(username = "test@email.com")
    void whenGetMyProfile_ShouldReturnProfileData() throws Exception {
        Profile mockProfile = Profile.builder()
                .username("test")
                .user(User.builder()
                        .email("test@email.com")
                        .role(Role.TITIPERS)
                        .build())
                .build();

        when(profileService.getOrCreateProfileByEmail(any()))
                .thenReturn(mockProfile);

        mockMvc.perform(get("/api/profile/me")
                        .principal(() -> "test@email.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("test"));
    }

    @Test
    @WithMockUser(username = "test@email.com")
    void whenGetMyProfile_ProfileNotFound_ShouldReturnNotFound() throws Exception {
        when(profileService.getOrCreateProfileByEmail(any()))
                .thenThrow(new ProfileNotFoundException("Profile not found"));

        mockMvc.perform(get("/api/profile/me")
                        .principal(() -> "test@email.com"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message")
                        .value("Profile not found"));
    }

    @Test
    void updateProfile_ShouldReturnUpdatedProfileResponse() {
        UpdateProfileRequest request = new UpdateProfileRequest(
                "new_username",
                "New Full Name",
                "This is a new bio"
        );

        Profile updatedProfile = Profile.builder()
                .id(UUID.randomUUID())
                .user(mockUser)
                .username(request.username())
                .fullName(request.fullName())
                .bio(request.bio())
                .successfulTransactions(0)
                .rating(0.0)
                .build();

        when(profileService.updateProfileByEmail(
                eq("test@email.com"),
                eq(request.username()),
                eq(request.fullName()),
                eq(request.bio())
        )).thenReturn(updatedProfile);

        ResponseEntity<ProfileResponse> response =
                profileController.updateProfile(principal, request);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());

        ProfileResponse body = response.getBody();

        assertEquals("new_username", body.username());
        assertEquals("New Full Name", body.fullName());
        assertEquals("This is a new bio", body.bio());
        assertEquals("test@email.com", body.email());

        verify(profileService, times(1)).updateProfileByEmail(
                "test@email.com",
                "new_username",
                "New Full Name",
                "This is a new bio"
        );
    }
}
