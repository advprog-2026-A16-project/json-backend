package id.ac.ui.cs.advprog.jsonbackend.auth.controller;

import id.ac.ui.cs.advprog.jsonbackend.auth.dto.KycSubmissionResponse;
import id.ac.ui.cs.advprog.jsonbackend.auth.enums.KycStatus;
import id.ac.ui.cs.advprog.jsonbackend.auth.model.KycSubmission;
import id.ac.ui.cs.advprog.jsonbackend.auth.dto.KycRequest;
import id.ac.ui.cs.advprog.jsonbackend.auth.model.User;
import id.ac.ui.cs.advprog.jsonbackend.auth.service.KycService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.Optional;
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
        mockUser.setEmail("titipers@example.com");

        KycRequest request = new KycRequest("Leon S. Kennedy", "3171234567890123", "https://instagram.com/leon");
        KycSubmission submission = KycSubmission.builder()
                .id(UUID.randomUUID())
                .user(mockUser)
                .kycFullName(request.fullName())
                .identityNumber(request.identityNumber())
                .socialMediaLink(request.socialMediaLink())
                .status(KycStatus.REQUESTED)
                .submittedAt(LocalDateTime.now())
                .build();
        org.mockito.Mockito.when(kycService.submitKyc(mockUser.getId(), request)).thenReturn(submission);

        ResponseEntity<KycSubmissionResponse> response = kycController.submitKyc(mockUser, request);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(submission.getId(), response.getBody().submissionId());
        verify(kycService, times(1)).submitKyc(eq(mockUser.getId()), eq(request));
    }

    @Test
    void getMyLatestKyc_ShouldReturnLatestSubmission() {
        User mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        mockUser.setEmail("titipers@example.com");
        KycSubmission submission = KycSubmission.builder()
                .id(UUID.randomUUID())
                .user(mockUser)
                .kycFullName("Leon S. Kennedy")
                .identityNumber("3171234567890123")
                .socialMediaLink("https://instagram.com/leon")
                .status(KycStatus.REQUESTED)
                .submittedAt(LocalDateTime.now())
                .build();
        org.mockito.Mockito.when(kycService.getLatestKycSubmission(mockUser.getId())).thenReturn(Optional.of(submission));

        ResponseEntity<KycSubmissionResponse> response = kycController.getMyLatestKyc(mockUser);

        assertEquals(200, response.getStatusCode().value());
        assertEquals(submission.getId(), response.getBody().submissionId());
    }

    @Test
    void getMyLatestKyc_ShouldReturnNoContentWhenMissing() {
        User mockUser = new User();
        mockUser.setId(UUID.randomUUID());
        org.mockito.Mockito.when(kycService.getLatestKycSubmission(mockUser.getId())).thenReturn(Optional.empty());

        ResponseEntity<KycSubmissionResponse> response = kycController.getMyLatestKyc(mockUser);

        assertEquals(204, response.getStatusCode().value());
    }
}
