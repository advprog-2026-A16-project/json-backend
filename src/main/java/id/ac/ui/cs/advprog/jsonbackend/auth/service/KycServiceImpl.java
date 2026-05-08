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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KycServiceImpl implements KycService {

    private final KycSubmissionRepository kycRepository;
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;

    @Override
    @Transactional
    public KycSubmission submitKyc(UUID userId, KycRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (user.getAccountStatus() == AccountStatus.PENDING_VERIFICATION) {
            throw new IllegalStateException("KYC application is being processed");
        }

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

        return kycRepository.save(submission);
    }

    @Override
    @Transactional(readOnly = true)
    public List<KycSubmission> getPendingKycList() {
        return kycRepository.findAllByStatus(KycStatus.REQUESTED);
    }

    @Override
    @Transactional
    public void approveKyc(UUID submissionId) {
        KycSubmission submission = kycRepository.findById(submissionId)
                .orElseThrow(() -> new KycSubmissionNotFoundException("KYC submission not found"));

        submission.setStatus(KycStatus.APPROVED);
        submission.setProcessedAt(LocalDateTime.now());
        kycRepository.save(submission);

        User user = submission.getUser();
        user.setRole(Role.JASTIPER);
        user.setAccountStatus(AccountStatus.ACTIVE);
        userRepository.save(user);
    }

    @Override
    @Transactional
    public void rejectKyc(UUID submissionId) {
        KycSubmission submission = kycRepository.findById(submissionId)
                .orElseThrow(() -> new KycSubmissionNotFoundException("KYC submission not found"));

        submission.setStatus(KycStatus.REJECTED);
        submission.setProcessedAt(LocalDateTime.now());
        kycRepository.save(submission);

        User user = submission.getUser();
        user.setAccountStatus(AccountStatus.ACTIVE);
        userRepository.save(user);
    }
}