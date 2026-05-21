package id.ac.ui.cs.advprog.jsonbackend.wallet.service;

import id.ac.ui.cs.advprog.jsonbackend.wallet.dto.PaymentGatewayTopUpResponse;
import id.ac.ui.cs.advprog.jsonbackend.wallet.dto.TopUpRequest;
import id.ac.ui.cs.advprog.jsonbackend.wallet.model.Transaction;
import id.ac.ui.cs.advprog.jsonbackend.wallet.model.TransactionStatus;
import id.ac.ui.cs.advprog.jsonbackend.wallet.model.TransactionType;
import id.ac.ui.cs.advprog.jsonbackend.wallet.model.Wallet;
import id.ac.ui.cs.advprog.jsonbackend.wallet.payment.PaymentGateway;
import id.ac.ui.cs.advprog.jsonbackend.wallet.payment.PaymentGatewayChargeRequest;
import id.ac.ui.cs.advprog.jsonbackend.wallet.payment.PaymentGatewayChargeResponse;
import id.ac.ui.cs.advprog.jsonbackend.wallet.repository.TransactionRepository;
import id.ac.ui.cs.advprog.jsonbackend.wallet.repository.WalletRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class WalletServiceImplThirdPartyPaymentTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private PaymentGateway paymentGateway;

    private WalletServiceImpl walletService;
    private UUID userId;
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        walletService = new WalletServiceImpl(walletRepository, transactionRepository, paymentGateway);
        userId = UUID.randomUUID();
        wallet = new Wallet();
        wallet.setUserId(userId);
        wallet.credit(new BigDecimal("100000"));
    }

    @Test
    void requestTopUpPaymentShouldCreatePendingTransactionAndGatewaySession() {
        UUID transactionId = UUID.randomUUID();
        TopUpRequest request = new TopUpRequest();
        request.setUserId(userId);
        request.setAmount(new BigDecimal("50000"));

        when(walletRepository.findByUserIdForUpdate(userId)).thenReturn(Optional.of(wallet));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> {
            Transaction transaction = invocation.getArgument(0);
            if (transaction.getId() == null) {
                transaction.setId(transactionId);
            }
            return transaction;
        });
        when(paymentGateway.createCharge(any(PaymentGatewayChargeRequest.class)))
                .thenReturn(new PaymentGatewayChargeResponse("snap-token", "https://pay.example/snap"));

        PaymentGatewayTopUpResponse response = walletService.requestTopUpPayment(request);

        assertEquals("snap-token", response.getPaymentToken());
        assertEquals("https://pay.example/snap", response.getPaymentRedirectUrl());
        assertEquals(TransactionStatus.PENDING, response.getTransaction().getStatus());
        assertEquals(TransactionType.TOP_UP, response.getTransaction().getType());
        assertEquals("MIDTRANS", response.getTransaction().getPaymentProvider());
        assertEquals("WALLET-TOPUP-" + transactionId, response.getTransaction().getGatewayOrderId());
        assertEquals(new BigDecimal("100000"), wallet.getBalance());

        ArgumentCaptor<PaymentGatewayChargeRequest> chargeRequestCaptor =
                ArgumentCaptor.forClass(PaymentGatewayChargeRequest.class);
        verify(paymentGateway).createCharge(chargeRequestCaptor.capture());
        assertEquals("WALLET-TOPUP-" + transactionId, chargeRequestCaptor.getValue().orderId());
        assertEquals(new BigDecimal("50000"), chargeRequestCaptor.getValue().amount());
        assertTrue(chargeRequestCaptor.getValue().description().contains("Top-up"));
        verify(walletRepository, never()).save(any());
        verify(transactionRepository, times(2)).save(any(Transaction.class));
    }
}
