package id.ac.ui.cs.advprog.jsonbackend.wallet.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "transactions")
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID userId;

    private BigDecimal amount;

    @Enumerated(EnumType.STRING)
    private TransactionType type;

    private LocalDateTime createdAt = LocalDateTime.now();

    public UUID getId() { return id; }

    public UUID getUserId() { return userId; }

    public void setUserId(UUID userId) { this.userId = userId; }

    public BigDecimal getAmount() { return amount; }

    public void setAmount(BigDecimal amount) { this.amount = amount; }

    public TransactionType getType() { return type; }

    public void setType(TransactionType type) { this.type = type; }
}