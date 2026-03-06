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
}