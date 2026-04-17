package id.ac.ui.cs.advprog.jsonbackend.wallet.service;

import id.ac.ui.cs.advprog.jsonbackend.wallet.dto.*;


public interface WalletService {

    WalletResponse topUp(TopUpRequest request);

    WalletResponse withdraw(WithdrawRequest request);

    WalletResponse payment(PaymentRequest request);

    WalletResponse refund(RefundRequest request);
}
