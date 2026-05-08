package id.ac.ui.cs.advprog.jsonbackend.auth.service;

import id.ac.ui.cs.advprog.jsonbackend.auth.enums.AccountStatus;
import id.ac.ui.cs.advprog.jsonbackend.auth.enums.Role;
import id.ac.ui.cs.advprog.jsonbackend.auth.exception.ProfileNotFoundException;
import id.ac.ui.cs.advprog.jsonbackend.auth.model.Profile;
import id.ac.ui.cs.advprog.jsonbackend.auth.model.User;
import id.ac.ui.cs.advprog.jsonbackend.auth.repository.ProfileRepository;
import id.ac.ui.cs.advprog.jsonbackend.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProfileRepository profileRepository;

    @InjectMocks
    private AdminUserServiceImpl adminUserService;

    private User user;
    private Profile profile;
    private UUID userId;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        user = User.builder()
                .id(userId)
                .email("test@email.com")
                .role(Role.TITIPERS)
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        profile = Profile.builder()
                .id(UUID.randomUUID())
                .user(user)
                .username("testuser")
                .build();
    }

    @Test
    void getAllUsers_ShouldReturnListOfProfiles() {
        when(profileRepository.findAll()).thenReturn(List.of(profile));

        List<Profile> profiles = adminUserService.getAllUsers();

        assertFalse(profiles.isEmpty());
        assertEquals(1, profiles.size());
        assertEquals("testuser", profiles.get(0).getUsername());
        verify(profileRepository, times(1)).findAll();
    }

    @Test
    void updateUserStatus_ShouldUpdateStatusAndRole_WhenValid() {
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.of(profile));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        Profile updatedProfile = adminUserService.updateUserStatus(userId, AccountStatus.BANNED, Role.ADMIN);

        assertNotNull(updatedProfile);
        assertEquals(AccountStatus.BANNED, updatedProfile.getUser().getAccountStatus());
        assertEquals(Role.ADMIN, updatedProfile.getUser().getRole());
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void updateUserStatus_ShouldThrowException_WhenProfileNotFound() {
        when(profileRepository.findByUserId(userId)).thenReturn(Optional.empty());

        assertThrows(ProfileNotFoundException.class,
                () -> adminUserService.updateUserStatus(userId, AccountStatus.ACTIVE, Role.JASTIPER));

        verify(userRepository, never()).save(any(User.class));
    }
}