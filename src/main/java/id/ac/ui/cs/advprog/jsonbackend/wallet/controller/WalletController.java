package id.ac.ui.cs.advprog.jsonbackend.wallet.controller;

import id.ac.ui.cs.advprog.jsonbackend.wallet.dto.*;
import id.ac.ui.cs.advprog.jsonbackend.wallet.exception.InsufficientBalanceException;
import id.ac.ui.cs.advprog.jsonbackend.wallet.exception.WalletNotFoundException;
import id.ac.ui.cs.advprog.jsonbackend.wallet.service.WalletService;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/wallet")
public class WalletController {

    private final WalletService walletService;

    public WalletController(WalletService walletService) {
        this.walletService = walletService;
    }

    @PostMapping("/top-up")
    public WalletResponse topUp(@RequestBody TopUpRequest request){
        return walletService.topUp(request);
    }

    @PostMapping("/withdraw")
    public WalletResponse withdraw(@RequestBody WithdrawRequest request){
        return walletService.withdraw(request);
    }

    @PostMapping("/payment")
    public WalletResponse payment(@RequestBody PaymentRequest request){
        return walletService.payment(request);
    }

    @PostMapping("/refund")
    public WalletResponse refund(@RequestBody RefundRequest request){
        return walletService.refund(request);
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
        throw new UnsupportedOperationException("belum diimplementasikan");
    }

    @ExceptionHandler(org.springframework.orm.ObjectOptimisticLockingFailureException.class)
    public org.springframework.http.ResponseEntity<java.util.Map<String, String>> handleRaceCondition(
            org.springframework.orm.ObjectOptimisticLockingFailureException e) {

        java.util.Map<String, String> response = new java.util.HashMap<>();
        response.put("error", "Sistem Sedang Sibuk");
        response.put("message", "Wah, antrean transaksimu tabrakan nih. Coba klik bayar sekali lagi ya!");

        return new org.springframework.http.ResponseEntity<>(response, org.springframework.http.HttpStatus.CONFLICT);
    }
}