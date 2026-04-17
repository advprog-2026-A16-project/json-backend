package id.ac.ui.cs.advprog.jsonbackend.wallet.controller;

import id.ac.ui.cs.advprog.jsonbackend.wallet.dto.*;
import id.ac.ui.cs.advprog.jsonbackend.wallet.service.WalletService;

import org.springframework.web.bind.annotation.*;

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

    @ExceptionHandler(org.springframework.orm.ObjectOptimisticLockingFailureException.class)
    public org.springframework.http.ResponseEntity<java.util.Map<String, String>> handleRaceCondition(
            org.springframework.orm.ObjectOptimisticLockingFailureException e) {

        java.util.Map<String, String> response = new java.util.HashMap<>();
        response.put("error", "Sistem Sedang Sibuk");
        response.put("message", "Wah, antrean transaksimu tabrakan nih. Coba klik bayar sekali lagi ya!");

        return new org.springframework.http.ResponseEntity<>(response, org.springframework.http.HttpStatus.CONFLICT);
    }
}