package id.ac.ui.cs.advprog.jsonbackend.auth.controller;

import id.ac.ui.cs.advprog.jsonbackend.auth.dto.AuthResponse;
import id.ac.ui.cs.advprog.jsonbackend.auth.dto.LoginRequest;
import id.ac.ui.cs.advprog.jsonbackend.auth.dto.RegisterRequest;
import id.ac.ui.cs.advprog.jsonbackend.auth.service.AuthService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @Test
    void registerReturnsOkStatusAndResponse() {
        RegisterRequest request = new RegisterRequest("test@example.com", "password", "password");
        AuthResponse expectedResponse = new AuthResponse("token", "Registrasi berhasil");
        when(authService.register(request)).thenReturn(expectedResponse);

        ResponseEntity<AuthResponse> response = authController.register(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
        verify(authService).register(request);
    }

    @Test
    void loginReturnsOkStatusAndResponse() {
        LoginRequest request = new LoginRequest("test@example.com", "password");
        AuthResponse expectedResponse = new AuthResponse("token", "Login berhasil");
        when(authService.login(request)).thenReturn(expectedResponse);

        ResponseEntity<AuthResponse> response = authController.login(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
        verify(authService).login(request);
    }
}