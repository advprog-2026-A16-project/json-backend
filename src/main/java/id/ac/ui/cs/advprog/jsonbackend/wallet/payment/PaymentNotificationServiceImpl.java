package id.ac.ui.cs.advprog.jsonbackend.wallet.payment;

import id.ac.ui.cs.advprog.jsonbackend.wallet.dto.MidtransNotificationRequest;
import id.ac.ui.cs.advprog.jsonbackend.wallet.dto.VerifyTransactionRequest;
import id.ac.ui.cs.advprog.jsonbackend.wallet.service.WalletService;
import org.springframework.stereotype.Service;

import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class PaymentNotificationServiceImpl implements PaymentNotificationService {
    private static final Set<String> SUCCESS_STATUSES = Set.of("settlement", "capture");
    private static final Set<String> FAILED_STATUSES = Set.of("deny", "cancel", "expire", "failure");

    private final WalletService walletService;
    private final MidtransNotificationVerifier verifier;

    public PaymentNotificationServiceImpl(WalletService walletService,
                                          MidtransNotificationVerifier verifier) {
        this.walletService = walletService;
        this.verifier = verifier;
    }

    @Override
    public void handleMidtransNotification(MidtransNotificationRequest notification) {
        if (!verifier.isValid(notification)) {
            throw new IllegalArgumentException("Invalid Midtrans notification signature");
        }

        Boolean success = verificationResult(notification);
        if (success == null) {
            return;
        }

        UUID transactionId = MidtransOrderId.extractTopUpTransactionId(notification.getOrderId());
        VerifyTransactionRequest request = new VerifyTransactionRequest();
        request.setSuccess(success);
        request.setDescription("Midtrans " + normalize(notification.getTransactionStatus()));

        walletService.verifyTransaction(transactionId, request);
    }

    private Boolean verificationResult(MidtransNotificationRequest notification) {
        String transactionStatus = normalize(notification.getTransactionStatus());
        String fraudStatus = normalize(notification.getFraudStatus());

        if ("deny".equals(fraudStatus)) {
            return false;
        }
        if (SUCCESS_STATUSES.contains(transactionStatus)
                && "200".equals(notification.getStatusCode())
                && (fraudStatus.isBlank() || "accept".equals(fraudStatus))) {
            return true;
        }
        if (FAILED_STATUSES.contains(transactionStatus)) {
            return false;
        }
        return null;
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase(Locale.ROOT);
    }
}
