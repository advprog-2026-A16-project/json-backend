package id.ac.ui.cs.advprog.jsonbackend.auth.controller;

import id.ac.ui.cs.advprog.jsonbackend.auth.dto.AuthResponse;
import id.ac.ui.cs.advprog.jsonbackend.auth.dto.LoginRequest;
import id.ac.ui.cs.advprog.jsonbackend.auth.dto.RegisterRequest;
import id.ac.ui.cs.advprog.jsonbackend.auth.exception.UserNotFoundException;
import id.ac.ui.cs.advprog.jsonbackend.auth.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private RegisterRequest registerRequest;
    private LoginRequest loginRequest;

    private AuthResponse registerResponse;
    private AuthResponse loginResponse;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest(
                "test@example.com",
                "password",
                "password"
        );

        loginRequest = new LoginRequest(
                "test@example.com",
                "password"
        );

        registerResponse = new AuthResponse(
                "token",
                "Registrasi berhasil"
        );

        loginResponse = new AuthResponse(
                "token",
                "Login berhasil"
        );
    }

    @Test
    void registerReturnsOkStatusAndResponse() {
        when(authService.register(registerRequest))
                .thenReturn(registerResponse);

        ResponseEntity<AuthResponse> response =
                authController.register(registerRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(registerResponse, response.getBody());

        verify(authService).register(registerRequest);
    }

    @Test
    void loginReturnsOkStatusAndResponse() {
        when(authService.login(loginRequest))
                .thenReturn(loginResponse);

        ResponseEntity<AuthResponse> response =
                authController.login(loginRequest);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(loginResponse, response.getBody());

        verify(authService).login(loginRequest);
    }

    @Test
    void loginThrowsUserNotFoundException() {
        when(authService.login(loginRequest))
                .thenThrow(new UserNotFoundException("User not found"));

        UserNotFoundException exception = assertThrows(
                UserNotFoundException.class,
                () -> authController.login(loginRequest)
        );

        assertEquals("User not found", exception.getMessage());

        verify(authService).login(loginRequest);
    }
}