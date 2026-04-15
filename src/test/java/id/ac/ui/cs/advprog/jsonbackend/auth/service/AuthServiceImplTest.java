package id.ac.ui.cs.advprog.jsonbackend.auth.service;

import id.ac.ui.cs.advprog.jsonbackend.auth.dto.AuthResponse;
import id.ac.ui.cs.advprog.jsonbackend.auth.dto.LoginRequest;
import id.ac.ui.cs.advprog.jsonbackend.auth.dto.RegisterRequest;
import id.ac.ui.cs.advprog.jsonbackend.auth.enums.Role;
import id.ac.ui.cs.advprog.jsonbackend.auth.model.User;
import id.ac.ui.cs.advprog.jsonbackend.auth.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private JwtService jwtService;
    @Mock
    private AuthenticationManager authenticationManager;

    @InjectMocks
    private AuthServiceImpl authService;

    private User user;

    @BeforeEach
    void setUp() {
        user = new User("test@example.com", "encodedPassword", Role.TITIPERS);
    }

    @Test
    void registerSuccess() {
        RegisterRequest request = new RegisterRequest("test@example.com", "password", "password");
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.password())).thenReturn("encodedPassword");
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");

        AuthResponse response = authService.register(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.token());
        verify(userRepository).save(any(User.class));
    }

    @Test
    void registerThrowsExceptionWhenPasswordMismatch() {
        RegisterRequest request = new RegisterRequest("test@example.com", "password", "wrong");

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> authService.register(request));
        assertEquals(HttpStatus.BAD_REQUEST, exception.getStatusCode());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerThrowsExceptionWhenEmailAlreadyExists() {
        RegisterRequest request = new RegisterRequest("test@example.com", "password", "password");
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));

        ResponseStatusException exception = assertThrows(ResponseStatusException.class, () -> authService.register(request));
        assertEquals(HttpStatus.CONFLICT, exception.getStatusCode());
        verify(userRepository, never()).save(any(User.class));
    }

    @Test
    void registerAssignsDefaultRoleWhenRoleIsNull() {
        RegisterRequest request = new RegisterRequest("test@example.com", "password", "password");
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(request.password())).thenReturn("encodedPassword");
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");

        authService.register(request);

        verify(userRepository).save(argThat(savedUser -> savedUser.getRole() == Role.TITIPERS));
    }

    @Test
    void loginSuccess() {
        LoginRequest request = new LoginRequest("test@example.com", "password");
        when(userRepository.findByEmail(request.email())).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        AuthResponse response = authService.login(request);

        assertNotNull(response);
        assertEquals("jwt-token", response.token());
        verify(authenticationManager).authenticate(any(UsernamePasswordAuthenticationToken.class));
    }
}