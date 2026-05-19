package id.ac.ui.cs.advprog.jsonbackend.wallet.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.*;

@Getter
@Setter
@Entity
@Table(
        name = "transactions",
        uniqueConstraints = @UniqueConstraint(
                name = "uk_wallet_transaction_reference",
                columnNames = {"user_id", "type", "reference_id"}
        )
)
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id", nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TransactionType type;

    @Column(name = "reference_id")
    private UUID referenceId;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}
