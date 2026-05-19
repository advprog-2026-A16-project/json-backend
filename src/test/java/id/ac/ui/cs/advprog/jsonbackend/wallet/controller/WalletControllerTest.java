package id.ac.ui.cs.advprog.jsonbackend.wallet.controller;

import id.ac.ui.cs.advprog.jsonbackend.auth.enums.Role;
import id.ac.ui.cs.advprog.jsonbackend.auth.model.User;
import id.ac.ui.cs.advprog.jsonbackend.wallet.dto.*;
import id.ac.ui.cs.advprog.jsonbackend.wallet.exception.InsufficientBalanceException;
import id.ac.ui.cs.advprog.jsonbackend.wallet.exception.WalletNotFoundException;
import id.ac.ui.cs.advprog.jsonbackend.wallet.model.Wallet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import id.ac.ui.cs.advprog.jsonbackend.wallet.service.WalletService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class WalletControllerTest {

    @Mock
    private WalletService walletService;

    @InjectMocks
    private WalletController walletController;

    private UUID userId;
    private User user;
    private User admin;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userId = UUID.randomUUID();

        user = new User();
        user.setId(userId);
        user.setRole(Role.TITIPERS);

        admin = new User();
        admin.setId(UUID.randomUUID());
        admin.setRole(Role.ADMIN);
    }

    @Test
    void testTopUpSuccess() {

        TopUpRequest request = new TopUpRequest();
        request.setUserId(userId);
        request.setAmount(new BigDecimal("50000"));

        WalletResponse mockResponse =
                new WalletResponse(userId, new BigDecimal("150000"));

        when(walletService.topUp(request)).thenReturn(mockResponse);

        WalletResponse response = walletController.topUp(user, request);

        assertNotNull(response);
        assertEquals(new BigDecimal("150000"), response.getBalance());

        verify(walletService, times(1)).topUp(request);
    }

    @Test
    void testWithdrawSuccess() {

        WithdrawRequest request = new WithdrawRequest();
        request.setUserId(userId);
        request.setAmount(new BigDecimal("20000"));
        request.setDestinationAccount("BCA 1234567890");

        WalletResponse mockResponse =
                new WalletResponse(userId, new BigDecimal("80000"));

        when(walletService.withdraw(request)).thenReturn(mockResponse);

        WalletResponse response = walletController.withdraw(user, request);

        assertNotNull(response);
        assertEquals(new BigDecimal("80000"), response.getBalance());

        verify(walletService, times(1)).withdraw(request);
    }

    @Test
    void testWithdrawThrowsException() {

        WithdrawRequest request = new WithdrawRequest();
        request.setUserId(userId);
        request.setAmount(new BigDecimal("200000"));
        request.setDestinationAccount("BCA 1234567890");

        when(walletService.withdraw(request))
                .thenThrow(new RuntimeException("Saldo tidak cukup"));

        assertThrows(RuntimeException.class, () -> {
            walletController.withdraw(user, request);
        });

        verify(walletService, times(1)).withdraw(request);
    }

    @Test
    void testPaymentSuccess() {
        PaymentRequest request = new PaymentRequest();
        request.setUserId(userId);
        request.setAmount(new BigDecimal("50000"));

        WalletResponse mockResponse =
                new WalletResponse(userId, new BigDecimal("50000"));

        when(walletService.payment(request)).thenReturn(mockResponse);

        WalletResponse response = walletController.payment(user, request);

        assertNotNull(response);
        assertEquals(new BigDecimal("50000"), response.getBalance());

        verify(walletService, times(1)).payment(request);
    }

    @Test
    void testRefundSuccess() {
        RefundRequest request = new RefundRequest();
        request.setUserId(userId);
        request.setAmount(new BigDecimal("50000"));

        WalletResponse mockResponse =
                new WalletResponse(userId, new BigDecimal("150000"));

        when(walletService.refund(request)).thenReturn(mockResponse);

        WalletResponse response = walletController.refund(admin, request);

        assertNotNull(response);
        assertEquals(new BigDecimal("150000"), response.getBalance());

        verify(walletService, times(1)).refund(request);
    }

    @Test
    void testRequestTopUpUsesAuthenticatedUser() {
        TopUpRequest request = new TopUpRequest();
        request.setUserId(UUID.randomUUID());
        request.setAmount(new BigDecimal("50000"));
        TransactionResponse mockResponse = new TransactionResponse();
        mockResponse.setUserId(userId);

        when(walletService.requestTopUp(request)).thenReturn(mockResponse);

        TransactionResponse response = walletController.requestTopUp(user, request);

        assertEquals(userId, response.getUserId());
        assertEquals(userId, request.getUserId());
        verify(walletService).requestTopUp(request);
    }

    @Test
    void testVerifyTransactionRequiresAdmin() {
        VerifyTransactionRequest request = new VerifyTransactionRequest();
        request.setSuccess(true);

        assertThrows(RuntimeException.class, () -> walletController.verifyTransaction(user, UUID.randomUUID(), request));

        verify(walletService, never()).verifyTransaction(any(), any());
    }

    @Test
    void testGetTransactionHistoryUsesAuthenticatedUser() {
        TransactionResponse response = new TransactionResponse();
        response.setUserId(userId);

        when(walletService.getTransactionHistory(userId)).thenReturn(List.of(response));

        List<TransactionResponse> result = walletController.getTransactionHistory(user);

        assertEquals(1, result.size());
        assertEquals(userId, result.get(0).getUserId());
        verify(walletService).getTransactionHistory(userId);
    }

    @Test
    void testHandleInsufficientBalance() {
        InsufficientBalanceException exception = new InsufficientBalanceException();

        ResponseEntity<Map<String, String>> response = walletController.handleInsufficientBalance(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());

        Map<String, String> body = response.getBody();
        assertNotNull(body);
        assertEquals("Saldo tidak mencukupi", body.get("error"));
        assertTrue(body.get("message").contains("Yuk top-up dulu"));
    }

    @Test
    void testHandleWalletNotFound() {
        WalletNotFoundException exception = new WalletNotFoundException();

        ResponseEntity<Map<String, String>> response = walletController.handleWalletNotFound(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        Map<String, String> body = response.getBody();
        assertNotNull(body);
        assertEquals("Wallet tidak ditemukan", body.get("error"));
        assertTrue(body.get("message").contains("belum aktif atau tidak ditemukan"));
    }

    @Test
    void testHandleRaceCondition() {
        ObjectOptimisticLockingFailureException exception =
                new ObjectOptimisticLockingFailureException(Wallet.class.getName(), "versi_konflik");

        ResponseEntity<Map<String, String>> response = walletController.handleRaceCondition(exception);

        assertNotNull(response);
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());

        Map<String, String> body = response.getBody();
        assertNotNull(body);
        assertEquals("Sistem Sedang Sibuk", body.get("error"));
        assertTrue(body.get("message").contains("antrean transaksimu tabrakan"));
    }
}
