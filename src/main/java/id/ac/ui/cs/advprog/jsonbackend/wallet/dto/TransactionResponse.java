package id.ac.ui.cs.advprog.jsonbackend.wallet.dto;

import id.ac.ui.cs.advprog.jsonbackend.wallet.model.Transaction;
import id.ac.ui.cs.advprog.jsonbackend.wallet.model.TransactionStatus;
import id.ac.ui.cs.advprog.jsonbackend.wallet.model.TransactionType;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
public class TransactionResponse {
    private UUID id;
    private UUID userId;
    private BigDecimal amount;
    private TransactionType type;
    private TransactionStatus status;
    private UUID referenceId;
    private String description;
    private String destinationAccount;
    private LocalDateTime createdAt;

    public static TransactionResponse from(Transaction transaction) {
        TransactionResponse response = new TransactionResponse();
        response.setId(transaction.getId());
        response.setUserId(transaction.getUserId());
        response.setAmount(transaction.getAmount());
        response.setType(transaction.getType());
        response.setStatus(transaction.getStatus() == null ? TransactionStatus.SUCCESS : transaction.getStatus());
        response.setReferenceId(transaction.getReferenceId());
        response.setDescription(transaction.getDescription());
        response.setDestinationAccount(transaction.getDestinationAccount());
        response.setCreatedAt(transaction.getCreatedAt());
        return response;
    }
}
