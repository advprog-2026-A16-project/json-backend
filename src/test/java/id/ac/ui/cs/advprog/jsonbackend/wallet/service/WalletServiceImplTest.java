package id.ac.ui.cs.advprog.jsonbackend.wallet.service;

import id.ac.ui.cs.advprog.jsonbackend.wallet.dto.*;
import id.ac.ui.cs.advprog.jsonbackend.wallet.exception.InsufficientBalanceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import id.ac.ui.cs.advprog.jsonbackend.wallet.model.Wallet;
import id.ac.ui.cs.advprog.jsonbackend.wallet.model.Transaction;
import id.ac.ui.cs.advprog.jsonbackend.wallet.model.TransactionStatus;
import id.ac.ui.cs.advprog.jsonbackend.wallet.model.TransactionType;
import id.ac.ui.cs.advprog.jsonbackend.wallet.payment.PaymentGateway;
import id.ac.ui.cs.advprog.jsonbackend.wallet.repository.TransactionRepository;
import id.ac.ui.cs.advprog.jsonbackend.wallet.repository.WalletRepository;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class WalletServiceImplTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionRepository transactionRepository;

    @Mock
    private PaymentGateway paymentGateway;

    @InjectMocks
    private WalletServiceImpl walletService;

    private UUID userId;
    private Wallet wallet;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        userId = UUID.randomUUID();

        wallet = new Wallet();
        wallet.setUserId(userId);
        wallet.credit(new BigDecimal("100000"));
    }

    @Test
    void testTopUpSuccess() {

        TopUpRequest request = new TopUpRequest();
        request.setUserId(userId);
        request.setAmount(new BigDecimal("50000"));

        when(walletRepository.findByUserIdForUpdate(userId))
                .thenReturn(Optional.of(wallet));

        WalletResponse response = walletService.topUp(request);

        assertEquals(new BigDecimal("150000"), response.getBalance());

        verify(walletRepository, times(1)).save(wallet);
        verify(transactionRepository, times(1)).save(any());
    }

    @Test
    void testWithdrawSuccess() {

        WithdrawRequest request = new WithdrawRequest();
        request.setUserId(userId);
        request.setAmount(new BigDecimal("30000"));
        request.setDestinationAccount("BCA 1234567890");

        when(walletRepository.findByUserIdForUpdate(userId))
                .thenReturn(Optional.of(wallet));

        WalletResponse response = walletService.withdraw(request);

        assertEquals(new BigDecimal("70000"), response.getBalance());

        verify(walletRepository, times(1)).save(wallet);
        verify(transactionRepository, times(1)).save(any());
    }

    @Test
    void testWithdrawInsufficientBalance() {

        WithdrawRequest request = new WithdrawRequest();
        request.setUserId(userId);
        request.setAmount(new BigDecimal("200000"));
        request.setDestinationAccount("BCA 1234567890");

        when(walletRepository.findByUserIdForUpdate(userId))
                .thenReturn(Optional.of(wallet));

        assertThrows(RuntimeException.class, () -> walletService.withdraw(request));

        verify(walletRepository, never()).save(wallet);
    }

    @Test
    void testPaymentSuccess() {
        PaymentRequest request = new PaymentRequest();
        request.setUserId(userId);
        request.setAmount(new BigDecimal("50000"));

        when(walletRepository.findByUserIdForUpdate(userId))
                .thenReturn(Optional.of(wallet));

        WalletResponse response = walletService.payment(request);

        assertEquals(new BigDecimal("50000"), response.getBalance());

        verify(walletRepository, times(1)).save(wallet);
        verify(transactionRepository, times(1)).save(any());
    }

    @Test
    void testPaymentInsufficientBalance() {
        PaymentRequest request = new PaymentRequest();
        request.setUserId(userId);
        request.setAmount(new BigDecimal("150000"));

        when(walletRepository.findByUserIdForUpdate(userId))
                .thenReturn(Optional.of(wallet));

        assertThrows(InsufficientBalanceException.class, () -> walletService.payment(request));

        verify(walletRepository, never()).save(any());
    }

    @Test
    void testRefundSuccess() {
        RefundRequest request = new RefundRequest();
        request.setUserId(userId);
        request.setAmount(new BigDecimal("50000"));

        when(walletRepository.findByUserIdForUpdate(userId))
                .thenReturn(Optional.of(wallet));

        WalletResponse response = walletService.refund(request);

        assertEquals(new BigDecimal("150000"), response.getBalance());

        verify(walletRepository, times(1)).save(wallet);
        verify(transactionRepository, times(1)).save(any());
    }

    @Test
    void testCreateWalletIfAbsentCreatesNewWallet() {
        when(walletRepository.findByUserId(userId)).thenReturn(Optional.empty());
        when(walletRepository.save(any(Wallet.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WalletResponse response = walletService.createWalletIfAbsent(userId);

        assertEquals(userId, response.getUserId());
        assertEquals(BigDecimal.ZERO, response.getBalance());
        verify(walletRepository).save(any(Wallet.class));
    }

    @Test
    void testPaymentForOrderIsIdempotent() {
        UUID orderId = UUID.randomUUID();
        Transaction existing = new Transaction();
        existing.setUserId(userId);
        existing.setAmount(new BigDecimal("50000"));
        existing.setType(TransactionType.PAYMENT);
        existing.setReferenceId(orderId);

        when(transactionRepository.findByUserIdAndTypeAndReferenceId(userId, TransactionType.PAYMENT, orderId))
                .thenReturn(Optional.of(existing));
        when(walletRepository.findByUserIdForUpdate(userId))
                .thenReturn(Optional.of(wallet));

        WalletResponse response = walletService.paymentForOrder(userId, new BigDecimal("50000"), orderId);

        assertEquals(new BigDecimal("100000"), response.getBalance());
        verify(walletRepository, never()).save(any());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void requestTopUpShouldCreatePendingTransactionWithoutChangingBalance() {
        TopUpRequest request = new TopUpRequest();
        request.setUserId(userId);
        request.setAmount(new BigDecimal("50000"));

        when(walletRepository.findByUserIdForUpdate(userId))
                .thenReturn(Optional.of(wallet));
        when(transactionRepository.save(any(Transaction.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TransactionResponse response = walletService.requestTopUp(request);

        assertEquals(TransactionStatus.PENDING, response.getStatus());
        assertEquals(TransactionType.TOP_UP, response.getType());
        assertEquals(new BigDecimal("50000"), response.getAmount());
        assertEquals(new BigDecimal("100000"), wallet.getBalance());
        verify(walletRepository, never()).save(wallet);
    }

    @Test
    void requestWithdrawalShouldRejectMissingDestinationAccount() {
        WithdrawRequest request = new WithdrawRequest();
        request.setUserId(userId);
        request.setAmount(new BigDecimal("50000"));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> walletService.requestWithdrawal(request)
        );

        assertEquals("Destination account is required", exception.getMessage());
        verify(transactionRepository, never()).save(any());
    }

    @Test
    void verifyPendingTopUpSuccessShouldCreditWalletAndMarkSuccess() {
        UUID transactionId = UUID.randomUUID();
        Transaction transaction = pendingTransaction(transactionId, TransactionType.TOP_UP, new BigDecimal("50000"));
        VerifyTransactionRequest request = new VerifyTransactionRequest();
        request.setSuccess(true);
        request.setDescription("Bank transfer received");

        when(transactionRepository.findById(transactionId)).thenReturn(Optional.of(transaction));
        when(walletRepository.findByUserIdForUpdate(userId)).thenReturn(Optional.of(wallet));
        when(transactionRepository.save(any(Transaction.class))).thenAnswer(invocation -> invocation.getArgument(0));

        TransactionResponse response = walletService.verifyTransaction(transactionId, request);

        assertEquals(TransactionStatus.SUCCESS, response.getStatus());
        assertEquals("Bank transfer received", response.getDescription());
        assertEquals(new BigDecimal("150000"), wallet.getBalance());
        verify(walletRepository).save(wallet);
    }

    @Test
    void getTransactionHistoryShouldReturnTransactionsForUser() {
        Transaction newest = successfulTransaction(UUID.randomUUID(), TransactionType.REFUND, new BigDecimal("25000"), "Refund order");
        Transaction oldest = successfulTransaction(UUID.randomUUID(), TransactionType.TOP_UP, new BigDecimal("100000"), "Initial top-up");

        when(transactionRepository.findByUserIdOrderByCreatedAtDesc(userId))
                .thenReturn(List.of(newest, oldest));

        List<TransactionResponse> responses = walletService.getTransactionHistory(userId);

        assertEquals(2, responses.size());
        assertEquals("Refund order", responses.get(0).getDescription());
        assertEquals(TransactionStatus.SUCCESS, responses.get(0).getStatus());
        assertNotNull(responses.get(0).getCreatedAt());
    }

    private Transaction pendingTransaction(UUID transactionId, TransactionType type, BigDecimal amount) {
        Transaction transaction = new Transaction();
        transaction.setId(transactionId);
        transaction.setUserId(userId);
        transaction.setAmount(amount);
        transaction.setType(type);
        transaction.setStatus(TransactionStatus.PENDING);
        transaction.setDescription("Pending verification");
        transaction.setCreatedAt(LocalDateTime.now());
        return transaction;
    }

    private Transaction successfulTransaction(UUID transactionId, TransactionType type, BigDecimal amount, String description) {
        Transaction transaction = pendingTransaction(transactionId, type, amount);
        transaction.setStatus(TransactionStatus.SUCCESS);
        transaction.setDescription(description);
        return transaction;
    }
}
