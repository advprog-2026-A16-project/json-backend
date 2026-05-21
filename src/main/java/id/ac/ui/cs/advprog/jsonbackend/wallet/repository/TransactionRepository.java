package id.ac.ui.cs.advprog.jsonbackend.wallet.repository;

import id.ac.ui.cs.advprog.jsonbackend.wallet.model.Transaction;
import id.ac.ui.cs.advprog.jsonbackend.wallet.model.TransactionStatus;
import id.ac.ui.cs.advprog.jsonbackend.wallet.model.TransactionType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TransactionRepository extends JpaRepository<Transaction, UUID> {
    Optional<Transaction> findByUserIdAndTypeAndReferenceId(UUID userId, TransactionType type, UUID referenceId);

    Optional<Transaction> findByUserIdAndTypeAndReferenceIdAndStatus(
            UUID userId,
            TransactionType type,
            UUID referenceId,
            TransactionStatus status
    );

    List<Transaction> findByUserIdOrderByCreatedAtDesc(UUID userId);
}
