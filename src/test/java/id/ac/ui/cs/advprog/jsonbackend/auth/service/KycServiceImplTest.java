package id.ac.ui.cs.advprog.jsonbackend.auth.service;

import id.ac.ui.cs.advprog.jsonbackend.auth.enums.AccountStatus;
import id.ac.ui.cs.advprog.jsonbackend.auth.enums.KycStatus;
import id.ac.ui.cs.advprog.jsonbackend.auth.enums.Role;
import id.ac.ui.cs.advprog.jsonbackend.auth.model.KycSubmission;
import id.ac.ui.cs.advprog.jsonbackend.auth.model.User;
import id.ac.ui.cs.advprog.jsonbackend.auth.repository.KycSubmissionRepository;
import id.ac.ui.cs.advprog.jsonbackend.auth.repository.ProfileRepository;
import id.ac.ui.cs.advprog.jsonbackend.auth.repository.UserRepository;
import id.ac.ui.cs.advprog.jsonbackend.auth.dto.KycRequest;
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
public class KycServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ProfileRepository profileRepository;

    @Mock
    private KycSubmissionRepository kycRepository;

    @InjectMocks
    private KycServiceImpl kycService;

    @Test
    void submitKyc_ShouldUpdateAccountStatusAndCreateSubmission_WhenDataIsValid() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .email("user@email.com")
                .role(Role.TITIPERS)
                .accountStatus(AccountStatus.ACTIVE)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);
        when(kycRepository.save(any(KycSubmission.class))).thenAnswer(i -> i.getArguments()[0]);

        KycRequest request = new KycRequest("Leon S. Kennedy", "3171234567890123", "https://instagram.com/leon");

        KycSubmission submission = kycService.submitKyc(userId, request);

        assertNotNull(submission);
        assertEquals(AccountStatus.PENDING_VERIFICATION, user.getAccountStatus());
        assertEquals(user, submission.getUser());
        assertEquals("Leon S. Kennedy", submission.getKycFullName());
        assertEquals("3171234567890123", submission.getIdentityNumber());
        assertEquals("https://instagram.com/leon", submission.getSocialMediaLink());
        assertEquals(KycStatus.REQUESTED, submission.getStatus());
        assertNotNull(submission.getSubmittedAt());

        verify(userRepository, times(1)).save(any(User.class));
        verify(kycRepository, times(1)).save(any(KycSubmission.class));
        verify(profileRepository, never()).save(any());
    }

    @Test
    void submitKyc_ShouldThrowException_WhenUserAlreadyPendingVerification() {
        UUID userId = UUID.randomUUID();
        User user = User.builder()
                .id(userId)
                .email("user@email.com")
                .role(Role.TITIPERS)
                .accountStatus(AccountStatus.PENDING_VERIFICATION)
                .build();

        when(userRepository.findById(userId)).thenReturn(Optional.of(user));

        KycRequest request = new KycRequest("Leon S. Kennedy", "3171234567890123", "https://instagram.com/leon");

        IllegalStateException exception = assertThrows(IllegalStateException.class,
                () -> kycService.submitKyc(userId, request));

        assertEquals("KYC application is being processed", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
        verify(kycRepository, never()).save(any(KycSubmission.class));
    }

    @Test
    void getPendingKycList_ShouldReturnListOfPendingSubmissions() {
        KycSubmission submission1 = KycSubmission.builder().status(KycStatus.REQUESTED).build();
        KycSubmission submission2 = KycSubmission.builder().status(KycStatus.REQUESTED).build();

        when(kycRepository.findAllByStatus(KycStatus.REQUESTED)).thenReturn(List.of(submission1, submission2));

        List<KycSubmission> result = kycService.getPendingKycList();

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(KycStatus.REQUESTED, result.get(0).getStatus());
        verify(kycRepository, times(1)).findAllByStatus(KycStatus.REQUESTED);
    }

    @Test
    void approveKyc_ShouldUpdateSubmissionStatusAndUserRole() {
        UUID submissionId = UUID.randomUUID();
        User user = User.builder()
                .id(UUID.randomUUID())
                .role(Role.TITIPERS)
                .accountStatus(AccountStatus.PENDING_VERIFICATION)
                .build();
        KycSubmission submission = KycSubmission.builder()
                .id(submissionId)
                .user(user)
                .status(KycStatus.REQUESTED)
                .build();

        when(kycRepository.findById(submissionId)).thenReturn(Optional.of(submission));
        when(kycRepository.save(any(KycSubmission.class))).thenAnswer(i -> i.getArgument(0));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        kycService.approveKyc(submissionId);

        assertEquals(KycStatus.APPROVED, submission.getStatus());
        assertNotNull(submission.getProcessedAt());

        assertEquals(Role.JASTIPER, user.getRole());
        assertEquals(AccountStatus.ACTIVE, user.getAccountStatus());

        verify(kycRepository, times(1)).save(submission);
        verify(userRepository, times(1)).save(user);
    }

    @Test
    void rejectKyc_ShouldUpdateSubmissionStatusAndRevertUserStatus() {
        UUID submissionId = UUID.randomUUID();
        User user = User.builder()
                .id(UUID.randomUUID())
                .role(Role.TITIPERS)
                .accountStatus(AccountStatus.PENDING_VERIFICATION)
                .build();
        KycSubmission submission = KycSubmission.builder()
                .id(submissionId)
                .user(user)
                .status(KycStatus.REQUESTED)
                .build();

        when(kycRepository.findById(submissionId)).thenReturn(Optional.of(submission));
        when(kycRepository.save(any(KycSubmission.class))).thenAnswer(i -> i.getArgument(0));
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArgument(0));

        kycService.rejectKyc(submissionId);

        assertEquals(KycStatus.REJECTED, submission.getStatus());
        assertNotNull(submission.getProcessedAt());

        assertEquals(Role.TITIPERS, user.getRole());
        assertEquals(AccountStatus.ACTIVE, user.getAccountStatus());

        verify(kycRepository, times(1)).save(submission);
        verify(userRepository, times(1)).save(user);
    }
}