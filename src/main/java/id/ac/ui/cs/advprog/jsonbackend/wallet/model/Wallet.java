package id.ac.ui.cs.advprog.jsonbackend.wallet.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.util.UUID;

import id.ac.ui.cs.advprog.jsonbackend.wallet.exception.InsufficientBalanceException;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
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

    @Version
    private Long version;

    private void validatePositiveAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
    }

    public void credit(BigDecimal amount) {
        validatePositiveAmount(amount);
        balance = balance.add(amount);
    }

    public void debit(BigDecimal amount) {
        validatePositiveAmount(amount);
        if (balance.compareTo(amount) < 0) {
            throw new InsufficientBalanceException();
        }
        balance = balance.subtract(amount);
    }
}