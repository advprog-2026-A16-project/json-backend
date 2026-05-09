package id.ac.ui.cs.advprog.jsonbackend.auth.controller;

import id.ac.ui.cs.advprog.jsonbackend.auth.dto.KycRequest;
import id.ac.ui.cs.advprog.jsonbackend.auth.model.User;
import id.ac.ui.cs.advprog.jsonbackend.auth.service.KycService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class KycControllerTest {

    @Mock
    private KycService kycService;

    @InjectMocks
    private KycController kycController;

    @Test
    void submitKyc_ShouldReturnOk() {
        User mockUser = new User();
        mockUser.setId(UUID.randomUUID());

        KycRequest request = new KycRequest("Leon S. Kennedy", "3171234567890123", "https://instagram.com/leon");

        ResponseEntity<String> response = kycController.submitKyc(mockUser, request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals("KYC submitted successfully", response.getBody());
        verify(kycService, times(1)).submitKyc(eq(mockUser.getId()), eq(request));
    }
}