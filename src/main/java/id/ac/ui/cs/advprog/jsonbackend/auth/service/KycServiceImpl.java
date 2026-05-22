package id.ac.ui.cs.advprog.jsonbackend.auth.service;

import id.ac.ui.cs.advprog.jsonbackend.auth.dto.KycRequest;
import id.ac.ui.cs.advprog.jsonbackend.auth.enums.AccountStatus;
import id.ac.ui.cs.advprog.jsonbackend.auth.enums.KycStatus;
import id.ac.ui.cs.advprog.jsonbackend.auth.enums.Role;
import id.ac.ui.cs.advprog.jsonbackend.auth.exception.KycSubmissionNotFoundException;
import id.ac.ui.cs.advprog.jsonbackend.auth.exception.UserNotFoundException;
import id.ac.ui.cs.advprog.jsonbackend.auth.model.KycSubmission;
import id.ac.ui.cs.advprog.jsonbackend.auth.model.Profile;
import id.ac.ui.cs.advprog.jsonbackend.auth.model.User;
import id.ac.ui.cs.advprog.jsonbackend.auth.repository.KycSubmissionRepository;
import id.ac.ui.cs.advprog.jsonbackend.auth.repository.ProfileRepository;
import id.ac.ui.cs.advprog.jsonbackend.auth.repository.UserRepository;
import id.ac.ui.cs.advprog.jsonbackend.common.monitoring.ApplicationMetrics;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KycServiceImpl implements KycService {

    private static final Logger log = LoggerFactory.getLogger(KycServiceImpl.class);

    private final KycSubmissionRepository kycRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;
    private final ApplicationMetrics applicationMetrics;

    @Override
    @Transactional
    public KycSubmission submitKyc(UUID userId, KycRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (user.getRole() != Role.TITIPERS) {
            throw new IllegalStateException("Only titipers accounts can submit KYC");
        }
        if (user.getAccountStatus() == AccountStatus.PENDING_VERIFICATION) {
            throw new IllegalStateException("KYC application is being processed");
        }
        kycRepository.findTopByUserIdOrderBySubmittedAtDesc(userId).ifPresent(latestSubmission -> {
            if (latestSubmission.getStatus() == KycStatus.REQUESTED) {
                throw new IllegalStateException("KYC application is being processed");
            }
            if (latestSubmission.getStatus() == KycStatus.APPROVED) {
                throw new IllegalStateException("KYC has already been approved");
            }
        });

        user.setAccountStatus(AccountStatus.PENDING_VERIFICATION);
        userRepository.save(user);

        KycSubmission submission = KycSubmission.builder()
                .user(user)
                .kycFullName(request.fullName())
                .identityNumber(request.identityNumber())
                .socialMediaLink(request.socialMediaLink())
                .status(KycStatus.REQUESTED)
                .submittedAt(LocalDateTime.now())
                .build();

        KycSubmission savedSubmission = kycRepository.save(submission);
        log.info("Auth event: KYC_SUBMITTED submissionId={} userId={}", savedSubmission.getId(), user.getId());
        return savedSubmission;
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<KycSubmission> getLatestKycSubmission(UUID userId) {
        return kycRepository.findTopByUserIdOrderBySubmittedAtDesc(userId);
    }

    @Override
    @Transactional(readOnly = true)
    public List<KycSubmission> getPendingKycList() {
        return kycRepository.findAllByStatus(KycStatus.REQUESTED);
    }

    @Override
    @Transactional
    public void approveKyc(UUID submissionId) {
        long startNanos = System.nanoTime();
        KycSubmission submission = kycRepository.findById(submissionId)
                .orElseThrow(() -> new KycSubmissionNotFoundException("KYC submission not found"));

        ensureRequestedSubmission(submission);
        submission.setStatus(KycStatus.APPROVED);
        submission.setProcessedAt(LocalDateTime.now());
        kycRepository.save(submission);

        User user = submission.getUser();
        user.setRole(Role.JASTIPER);
        user.setAccountStatus(AccountStatus.ACTIVE);
        userRepository.save(user);
        log.info("Auth event: KYC_APPROVED submissionId={} userId={}", submissionId, user.getId());
        applicationMetrics.recordKycApprove(elapsed(startNanos));
    }

    @Override
    @Transactional
    public void rejectKyc(UUID submissionId) {
        long startNanos = System.nanoTime();
        KycSubmission submission = kycRepository.findById(submissionId)
                .orElseThrow(() -> new KycSubmissionNotFoundException("KYC submission not found"));

        ensureRequestedSubmission(submission);
        submission.setStatus(KycStatus.REJECTED);
        submission.setProcessedAt(LocalDateTime.now());
        kycRepository.save(submission);

        User user = submission.getUser();
        user.setAccountStatus(AccountStatus.ACTIVE);
        userRepository.save(user);
        log.info("Auth event: KYC_REJECTED submissionId={} userId={}", submissionId, user.getId());
        applicationMetrics.recordKycReject(elapsed(startNanos));
    }

    private void ensureRequestedSubmission(KycSubmission submission) {
        if (submission.getStatus() != KycStatus.REQUESTED) {
            throw new IllegalStateException("KYC submission has already been processed");
        }
    }

    private Duration elapsed(long startNanos) {
        return Duration.ofNanos(System.nanoTime() - startNanos);
    }
}
