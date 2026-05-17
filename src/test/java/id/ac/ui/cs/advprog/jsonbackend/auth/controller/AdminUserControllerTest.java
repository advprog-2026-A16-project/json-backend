package id.ac.ui.cs.advprog.jsonbackend.auth.controller;

import id.ac.ui.cs.advprog.jsonbackend.auth.dto.AdminUserResponse;
import id.ac.ui.cs.advprog.jsonbackend.auth.dto.KycSubmissionResponse;
import id.ac.ui.cs.advprog.jsonbackend.auth.dto.UserStatusUpdateRequest;
import id.ac.ui.cs.advprog.jsonbackend.auth.enums.AccountStatus;
import id.ac.ui.cs.advprog.jsonbackend.auth.enums.KycStatus;
import id.ac.ui.cs.advprog.jsonbackend.auth.enums.Role;
import id.ac.ui.cs.advprog.jsonbackend.auth.model.KycSubmission;
import id.ac.ui.cs.advprog.jsonbackend.auth.model.Profile;
import id.ac.ui.cs.advprog.jsonbackend.auth.model.User;
import id.ac.ui.cs.advprog.jsonbackend.auth.service.AdminUserService;
import id.ac.ui.cs.advprog.jsonbackend.auth.service.KycService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminUserControllerTest {

    @Mock
    private KycService kycService;

    @Mock
    private AdminUserService adminUserService;

    @InjectMocks
    private AdminUserController adminUserController;

    private User user;
    private Profile profile;
    private KycSubmission submission;

    @BeforeEach
    void setUp() {
        user = User.builder().id(UUID.randomUUID()).email("test@mail.com").role(Role.TITIPERS).accountStatus(AccountStatus.ACTIVE).build();
        profile = Profile.builder().id(UUID.randomUUID()).user(user).username("testuser").build();
        submission = KycSubmission.builder()
                .id(UUID.randomUUID())
                .user(user)
                .kycFullName("Real Name")
                .identityNumber("123456")
                .status(KycStatus.REQUESTED)
                .submittedAt(LocalDateTime.now())
                .build();
    }

    @Test
    void getAllUsers_ShouldReturnAdminUserResponseList() {
        when(adminUserService.getAllUsers()).thenReturn(List.of(profile));

        ResponseEntity<List<AdminUserResponse>> response = adminUserController.getAllUsers();

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("testuser", response.getBody().get(0).username());
    }

    @Test
    void updateUserStatus_ShouldReturnUpdatedProfile() {
        UserStatusUpdateRequest request = new UserStatusUpdateRequest(AccountStatus.BANNED, Role.TITIPERS);
        when(adminUserService.updateUserStatus(any(UUID.class), any(), any())).thenReturn(profile);

        ResponseEntity<AdminUserResponse> response = adminUserController.updateUserStatus(user.getId(), request);

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        verify(adminUserService).updateUserStatus(user.getId(), AccountStatus.BANNED, Role.TITIPERS);
    }

    @Test
    void getPendingKyc_ShouldReturnKycSubmissionResponseList() {
        when(kycService.getPendingKycList()).thenReturn(List.of(submission));

        ResponseEntity<List<KycSubmissionResponse>> response = adminUserController.getPendingKyc();

        assertEquals(200, response.getStatusCode().value());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().size());
        assertEquals("Real Name", response.getBody().get(0).kycFullName());
    }

    @Test
    void approveKyc_ShouldReturnSuccessMessage() {
        doNothing().when(kycService).approveKyc(submission.getId());

        ResponseEntity<String> response = adminUserController.approveKyc(submission.getId());

        assertEquals(200, response.getStatusCode().value());
        assertEquals("KYC approved successfully", response.getBody());
        verify(kycService, times(1)).approveKyc(submission.getId());
    }

    @Test
    void rejectKyc_ShouldReturnSuccessMessage() {
        doNothing().when(kycService).rejectKyc(submission.getId());

        ResponseEntity<String> response = adminUserController.rejectKyc(submission.getId());

        assertEquals(200, response.getStatusCode().value());
        assertEquals("KYC rejected successfully", response.getBody());
        verify(kycService, times(1)).rejectKyc(submission.getId());
    }
}