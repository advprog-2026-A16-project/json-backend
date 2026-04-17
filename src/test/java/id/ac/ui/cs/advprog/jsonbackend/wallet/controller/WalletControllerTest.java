package id.ac.ui.cs.advprog.jsonbackend.wallet.controller;

import id.ac.ui.cs.advprog.jsonbackend.wallet.model.Wallet;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import id.ac.ui.cs.advprog.jsonbackend.wallet.dto.TopUpRequest;
import id.ac.ui.cs.advprog.jsonbackend.wallet.dto.WithdrawRequest;
import id.ac.ui.cs.advprog.jsonbackend.wallet.dto.WalletResponse;
import id.ac.ui.cs.advprog.jsonbackend.wallet.service.WalletService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import java.math.BigDecimal;
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

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        userId = UUID.randomUUID();
    }

    // =========================
    // TEST TOP UP
    // =========================
    @Test
    void testTopUpSuccess() {

        TopUpRequest request = new TopUpRequest();
        request.setUserId(userId);
        request.setAmount(new BigDecimal("50000"));

        WalletResponse mockResponse =
                new WalletResponse(userId, new BigDecimal("150000"));

        when(walletService.topUp(request)).thenReturn(mockResponse);

        WalletResponse response = walletController.topUp(request);

        assertNotNull(response);
        assertEquals(new BigDecimal("150000"), response.getBalance());

        verify(walletService, times(1)).topUp(request);
    }

    // =========================
    // TEST WITHDRAW
    // =========================
    @Test
    void testWithdrawSuccess() {

        WithdrawRequest request = new WithdrawRequest();
        request.setUserId(userId);
        request.setAmount(new BigDecimal("20000"));

        WalletResponse mockResponse =
                new WalletResponse(userId, new BigDecimal("80000"));

        when(walletService.withdraw(request)).thenReturn(mockResponse);

        WalletResponse response = walletController.withdraw(request);

        assertNotNull(response);
        assertEquals(new BigDecimal("80000"), response.getBalance());

        verify(walletService, times(1)).withdraw(request);
    }

    // =========================
    // TEST SERVICE EXCEPTION
    // =========================
    @Test
    void testWithdrawThrowsException() {

        WithdrawRequest request = new WithdrawRequest();
        request.setUserId(userId);
        request.setAmount(new BigDecimal("200000"));

        when(walletService.withdraw(request))
                .thenThrow(new RuntimeException("Saldo tidak cukup"));

        assertThrows(RuntimeException.class, () -> {
            walletController.withdraw(request);
        });

        verify(walletService, times(1)).withdraw(request);
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