package id.ac.ui.cs.advprog.jsonbackend.auth.dto;

import id.ac.ui.cs.advprog.jsonbackend.auth.enums.KycStatus;
import id.ac.ui.cs.advprog.jsonbackend.auth.model.KycSubmission;

import java.time.LocalDateTime;
import java.util.UUID;

public record KycSubmissionResponse(
        UUID submissionId,
        UUID userId,
        String email,
        String kycFullName,
        String identityNumber,
        String socialMediaLink,
        KycStatus status,
        LocalDateTime submittedAt,
        LocalDateTime processedAt
) {
    public static KycSubmissionResponse from(KycSubmission submission) {
        return new KycSubmissionResponse(
                submission.getId(),
                submission.getUser().getId(),
                submission.getUser().getEmail(),
                submission.getKycFullName(),
                submission.getIdentityNumber(),
                submission.getSocialMediaLink(),
                submission.getStatus(),
                submission.getSubmittedAt(),
                submission.getProcessedAt()
        );
    }
}