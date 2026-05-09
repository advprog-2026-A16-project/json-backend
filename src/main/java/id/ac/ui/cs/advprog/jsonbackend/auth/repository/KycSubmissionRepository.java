package id.ac.ui.cs.advprog.jsonbackend.auth.repository;

import id.ac.ui.cs.advprog.jsonbackend.auth.enums.KycStatus;
import id.ac.ui.cs.advprog.jsonbackend.auth.model.KycSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface KycSubmissionRepository extends JpaRepository<KycSubmission, UUID> {
    List<KycSubmission> findAllByStatus(KycStatus status);
    Optional<KycSubmission> findTopByUserIdOrderBySubmittedAtDesc(UUID userId);
}