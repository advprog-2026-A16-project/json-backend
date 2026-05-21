package id.ac.ui.cs.advprog.jsonbackend.auth.service;

import id.ac.ui.cs.advprog.jsonbackend.auth.dto.AuthResponse;
import id.ac.ui.cs.advprog.jsonbackend.auth.dto.LoginRequest;
import id.ac.ui.cs.advprog.jsonbackend.auth.dto.RegisterRequest;
import id.ac.ui.cs.advprog.jsonbackend.auth.enums.AccountStatus;
import id.ac.ui.cs.advprog.jsonbackend.auth.enums.Role;
import id.ac.ui.cs.advprog.jsonbackend.auth.event.model.AuthOutboxEvent;
import id.ac.ui.cs.advprog.jsonbackend.auth.event.repository.AuthOutboxEventRepository;
import id.ac.ui.cs.advprog.jsonbackend.auth.exception.AccountBannedException;
import id.ac.ui.cs.advprog.jsonbackend.auth.exception.EmailAlreadyRegisteredException;
import id.ac.ui.cs.advprog.jsonbackend.auth.exception.PasswordMismatchException;
import id.ac.ui.cs.advprog.jsonbackend.auth.exception.UserNotFoundException;
import id.ac.ui.cs.advprog.jsonbackend.auth.model.User;
import id.ac.ui.cs.advprog.jsonbackend.auth.model.Profile;
import id.ac.ui.cs.advprog.jsonbackend.auth.repository.UserRepository;
import id.ac.ui.cs.advprog.jsonbackend.common.monitoring.ApplicationMetrics;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProfileService profileService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtService jwtService;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private AuthOutboxEventRepository outboxEventRepository;

    @Mock
    private ApplicationMetrics applicationMetrics;

    @InjectMocks
    private AuthServiceImpl authService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("test@email.com", "encodedPassword", Role.TITIPERS);
        user.setId(UUID.randomUUID());
    }

    @Test
    void registerSuccess() {
        RegisterRequest request = new RegisterRequest("test@email.com", "password", "password");
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.password())).thenReturn("encodedPassword");

        doAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        }).when(userRepository).save(any(User.class));

        Profile mockProfile = Profile.builder()
                .user(user)
                .username("test")
                .build();
        mockProfile.setId(UUID.randomUUID());
        when(profileService.createProfileForUser(any(User.class), anyString())).thenReturn(mockProfile);

        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.token());

        verify(userRepository).save(any(User.class));
        verify(profileService).createProfileForUser(any(User.class), anyString());
        verify(outboxEventRepository).save(any(AuthOutboxEvent.class));
        verify(applicationMetrics).recordRegisterSuccess(any(Duration.class));
    }

    @Test
    void registerThrowsExceptionWhenPasswordMismatch() {
        RegisterRequest request = new RegisterRequest("test@email.com", "password", "wrong");

        assertThrows(PasswordMismatchException.class, () -> authService.register(request));

        verify(userRepository, never()).save(any(User.class));
        verify(profileService, never()).createProfileForUser(any(), any());
        verify(outboxEventRepository, never()).save(any(AuthOutboxEvent.class));
    }

    @Test
    void registerThrowsExceptionWhenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest("test@email.com", "password", "password");
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));

        assertThrows(EmailAlreadyRegisteredException.class, () -> authService.register(request));

        verify(userRepository, never()).save(any(User.class));
        verify(profileService, never()).createProfileForUser(any(), any());
        verify(outboxEventRepository, never()).save(any(AuthOutboxEvent.class));
    }

    @Test
    void registerAssignsDefaultRoleWhenRoleIsNull() {
        RegisterRequest request = new RegisterRequest("test@email.com", "password", "password");
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.password())).thenReturn("encodedPassword");

        doAnswer(invocation -> {
            User u = invocation.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        }).when(userRepository).save(any(User.class));
        
        Profile mockProfile = Profile.builder()
                .user(user)
                .username("test")
                .build();
        mockProfile.setId(UUID.randomUUID());
        when(profileService.createProfileForUser(any(User.class), anyString())).thenReturn(mockProfile);

        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");

        authService.register(request);

        verify(userRepository).save(argThat(savedUser -> savedUser.getRole() == Role.TITIPERS));
        verify(profileService).createProfileForUser(any(User.class), anyString()); // VERIFY UPDATE
        verify(outboxEventRepository).save(any(AuthOutboxEvent.class));
    }

    @Test
    void loginSuccess() {
        LoginRequest request = new LoginRequest("test@email.com", "password");
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.token());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
        verify(applicationMetrics).recordLoginSuccess(any(Duration.class));
    }

    @Test
    void whenLoginWithBannedAccount_thenThrowAccountBannedException() {
        LoginRequest request = new LoginRequest("banned@email.com", "password");
        User bannedUser = User.builder()
                .email("banned@email.com")
                .password("encoded_password")
                .role(Role.TITIPERS)
                .accountStatus(AccountStatus.BANNED)
                .build();

        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(bannedUser));

        assertThrows(AccountBannedException.class, () -> authService.login(request));

        verify(jwtService, never()).generateToken(any(User.class));
        verify(applicationMetrics).recordLoginFailure(any(Duration.class));
    }

    @Test
    void testChangePasswordSuccess() {
        ChangePasswordRequest request = new ChangePasswordRequest("oldPass", "newPass");
        User user = User.builder()
                .username("testuser")
                .password("encodedOldPass")
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("oldPass", "encodedOldPass")).thenReturn(true);
        when(passwordEncoder.encode("newPass")).thenReturn("encodedNewPass");

        authService.changePassword("testuser", request);

        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder).matches("oldPass", "encodedOldPass");
        verify(passwordEncoder).encode("newPass");
        verify(userRepository).save(user);

        assertEquals("encodedNewPass", user.getPassword());
    }

    @Test
    void testChangePasswordWrongOldPassword() {
        ChangePasswordRequest request = new ChangePasswordRequest("wrongOldPass", "newPass");
        User user = User.builder()
                .username("testuser")
                .password("encodedOldPass")
                .build();

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("wrongOldPass", "encodedOldPass")).thenReturn(false);

        assertThrows(PasswordMismatchException.class, () ->
                authService.changePassword("testuser", request)
        );

        verify(userRepository).findByUsername("testuser");
        verify(passwordEncoder).matches("wrongOldPass", "encodedOldPass");
        verify(passwordEncoder, never()).encode(anyString());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void testChangePasswordUserNotFound() {
        ChangePasswordRequest request = new ChangePasswordRequest("oldPass", "newPass");

        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(UserNotFoundException.class, () ->
                authService.changePassword("unknown", request)
        );

        verify(userRepository).findByUsername("unknown");
        verify(passwordEncoder, never()).matches(anyString(), anyString());
    }
}
