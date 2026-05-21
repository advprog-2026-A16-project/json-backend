package id.ac.ui.cs.advprog.jsonbackend.wallet.controller;

import id.ac.ui.cs.advprog.jsonbackend.auth.enums.Role;
import id.ac.ui.cs.advprog.jsonbackend.auth.model.User;
import id.ac.ui.cs.advprog.jsonbackend.wallet.dto.*;
import id.ac.ui.cs.advprog.jsonbackend.wallet.exception.InsufficientBalanceException;
import id.ac.ui.cs.advprog.jsonbackend.wallet.exception.WalletNotFoundException;
import id.ac.ui.cs.advprog.jsonbackend.wallet.payment.PaymentNotificationService;
import id.ac.ui.cs.advprog.jsonbackend.wallet.service.WalletService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    private final WalletService walletService;
    private final PaymentNotificationService paymentNotificationService;

    public WalletController(WalletService walletService,
                            PaymentNotificationService paymentNotificationService) {
        this.walletService = walletService;
        this.paymentNotificationService = paymentNotificationService;
    }

    @GetMapping
    public WalletResponse getWallet(@AuthenticationPrincipal User user) {
        return walletService.createWalletIfAbsent(authenticatedUserId(user));
    }

    @PostMapping("/top-up")
    public WalletResponse topUp(@AuthenticationPrincipal User user, @Valid @RequestBody TopUpRequest request){
        request.setUserId(authenticatedUserId(user));
        return walletService.topUp(request);
    }

    @PostMapping("/top-up/request")
    public TransactionResponse requestTopUp(@AuthenticationPrincipal User user, @Valid @RequestBody TopUpRequest request){
        request.setUserId(authenticatedUserId(user));
        return walletService.requestTopUp(request);
    }

    @PostMapping("/top-up/payment")
    public PaymentGatewayTopUpResponse requestTopUpPayment(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody TopUpRequest request) {
        request.setUserId(authenticatedUserId(user));
        return walletService.requestTopUpPayment(request);
    }

    @PostMapping("/withdraw")
    public WalletResponse withdraw(@AuthenticationPrincipal User user, @Valid @RequestBody WithdrawRequest request){
        request.setUserId(authenticatedUserId(user));
        return walletService.withdraw(request);
    }

    @PostMapping("/withdraw/request")
    public TransactionResponse requestWithdrawal(@AuthenticationPrincipal User user, @Valid @RequestBody WithdrawRequest request){
        request.setUserId(authenticatedUserId(user));
        return walletService.requestWithdrawal(request);
    }

    @PostMapping("/payment")
    public WalletResponse payment(@AuthenticationPrincipal User user, @Valid @RequestBody PaymentRequest request){
        request.setUserId(authenticatedUserId(user));
        return walletService.payment(request);
    }

    @PostMapping("/refund")
    public WalletResponse refund(@AuthenticationPrincipal User user, @Valid @RequestBody RefundRequest request){
        requireAdmin(user);
        return walletService.refund(request);
    }

    @PatchMapping("/transactions/{transactionId}/verify")
    public TransactionResponse verifyTransaction(
            @AuthenticationPrincipal User user,
            @PathVariable UUID transactionId,
            @Valid @RequestBody VerifyTransactionRequest request) {
        requireAdmin(user);
        return walletService.verifyTransaction(transactionId, request);
    }

    @GetMapping("/transactions")
    public List<TransactionResponse> getTransactionHistory(@AuthenticationPrincipal User user) {
        return walletService.getTransactionHistory(authenticatedUserId(user));
    }

    @PostMapping("/payments/midtrans/notifications")
    public ResponseEntity<Map<String, String>> handleMidtransNotification(
            @RequestBody MidtransNotificationRequest request) {
        paymentNotificationService.handleMidtransNotification(request);
        return ResponseEntity.ok(Map.of("status", "ok"));
    }

    private UUID authenticatedUserId(User user) {
        if (user == null || user.getId() == null) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Authenticated user is required");
        }
        return user.getId();
    }

    private void requireAdmin(User user) {
        if (user == null || user.getRole() != Role.ADMIN) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Admin access required");
        }
    }

    @ExceptionHandler(InsufficientBalanceException.class)
    public ResponseEntity<Map<String, String>> handleInsufficientBalance(InsufficientBalanceException e) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "Saldo tidak mencukupi");
        response.put("message", "Waduh, saldo kamu nggak cukup nih. Yuk top-up dulu biar bisa lanjut war!");
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(WalletNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleWalletNotFound(WalletNotFoundException e) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "Wallet tidak ditemukan");
        response.put("message", "Dompet digital kamu belum aktif atau tidak ditemukan.");
        return new ResponseEntity<>(response, HttpStatus.NOT_FOUND);
    }

    @ExceptionHandler(org.springframework.orm.ObjectOptimisticLockingFailureException.class)
    public org.springframework.http.ResponseEntity<java.util.Map<String, String>> handleRaceCondition(
            org.springframework.orm.ObjectOptimisticLockingFailureException e) {

        java.util.Map<String, String> response = new java.util.HashMap<>();
        response.put("error", "Sistem Sedang Sibuk");
        response.put("message", "Wah, antrean transaksimu tabrakan nih. Coba klik bayar sekali lagi ya!");

        return new org.springframework.http.ResponseEntity<>(response, org.springframework.http.HttpStatus.CONFLICT);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, String>> handleInvalidWalletRequest(IllegalArgumentException e) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "Request tidak valid");
        response.put("message", e.getMessage());
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
