package id.ac.ui.cs.advprog.jsonbackend.wallet.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "wallets")
public class Wallet {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(unique = true, nullable = false)
    private UUID userId;

    @Column(nullable = false)
    private BigDecimal balance = BigDecimal.ZERO;

    public UUID getId() { return id; }

    public UUID getUserId() { return userId; }

    public void setUserId(UUID userId) { this.userId = userId; }

    public BigDecimal getBalance() { return balance; }

    public void credit(BigDecimal amount) {
        balance = balance.add(amount);
    }

    public void debit(BigDecimal amount) {
        if(balance.compareTo(amount) < 0){
            throw new RuntimeException("Saldo tidak cukup");
        }
        balance = balance.subtract(amount);
    }
}