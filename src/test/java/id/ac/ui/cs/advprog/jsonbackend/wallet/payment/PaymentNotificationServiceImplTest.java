package id.ac.ui.cs.advprog.jsonbackend.wallet.payment;

import id.ac.ui.cs.advprog.jsonbackend.wallet.dto.MidtransNotificationRequest;
import id.ac.ui.cs.advprog.jsonbackend.wallet.dto.VerifyTransactionRequest;
import id.ac.ui.cs.advprog.jsonbackend.wallet.service.WalletService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class PaymentNotificationServiceImplTest {

    @Mock
    private WalletService walletService;

    @Mock
    private MidtransNotificationVerifier verifier;

    private PaymentNotificationServiceImpl service;
    private UUID transactionId;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        service = new PaymentNotificationServiceImpl(walletService, verifier);
        transactionId = UUID.randomUUID();
    }

    @Test
    void handleMidtransNotificationShouldMarkTopUpSuccessOnSettlement() {
        MidtransNotificationRequest notification = notification("settlement", "200", null);
        when(verifier.isValid(notification)).thenReturn(true);

        service.handleMidtransNotification(notification);

        ArgumentCaptor<VerifyTransactionRequest> captor =
                ArgumentCaptor.forClass(VerifyTransactionRequest.class);
        verify(walletService).verifyTransaction(eq(transactionId), captor.capture());
        assertTrue(captor.getValue().getSuccess());
        assertTrue(captor.getValue().getDescription().contains("Midtrans settlement"));
    }

    @Test
    void handleMidtransNotificationShouldMarkTopUpFailedOnExpire() {
        MidtransNotificationRequest notification = notification("expire", "407", null);
        when(verifier.isValid(notification)).thenReturn(true);

        service.handleMidtransNotification(notification);

        ArgumentCaptor<VerifyTransactionRequest> captor =
                ArgumentCaptor.forClass(VerifyTransactionRequest.class);
        verify(walletService).verifyTransaction(eq(transactionId), captor.capture());
        assertFalse(captor.getValue().getSuccess());
    }

    @Test
    void handleMidtransNotificationShouldIgnorePendingStatus() {
        MidtransNotificationRequest notification = notification("pending", "201", null);
        when(verifier.isValid(notification)).thenReturn(true);

        service.handleMidtransNotification(notification);

        verify(walletService, never()).verifyTransaction(any(), any());
    }

    @Test
    void handleMidtransNotificationShouldRejectInvalidSignature() {
        MidtransNotificationRequest notification = notification("settlement", "200", null);
        when(verifier.isValid(notification)).thenReturn(false);

        assertThrows(IllegalArgumentException.class, () -> service.handleMidtransNotification(notification));

        verify(walletService, never()).verifyTransaction(any(), any());
    }

    private MidtransNotificationRequest notification(String transactionStatus, String statusCode, String fraudStatus) {
        MidtransNotificationRequest notification = new MidtransNotificationRequest();
        notification.setOrderId("WALLET-TOPUP-" + transactionId);
        notification.setStatusCode(statusCode);
        notification.setGrossAmount("50000.00");
        notification.setSignatureKey("signature");
        notification.setTransactionStatus(transactionStatus);
        notification.setFraudStatus(fraudStatus);
        return notification;
    }
}
