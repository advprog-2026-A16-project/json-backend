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
import id.ac.ui.cs.advprog.jsonbackend.wallet.model.TransactionType;
import id.ac.ui.cs.advprog.jsonbackend.wallet.repository.TransactionRepository;
import id.ac.ui.cs.advprog.jsonbackend.wallet.repository.WalletRepository;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class WalletServiceImplTest {

    @Mock
    private WalletRepository walletRepository;

    @Mock
    private TransactionRepository transactionRepository;

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
}
