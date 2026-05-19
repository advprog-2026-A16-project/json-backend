package id.ac.ui.cs.advprog.jsonbackend.wallet.service;

import id.ac.ui.cs.advprog.jsonbackend.wallet.dto.*;

import java.math.BigDecimal;
import java.util.UUID;


public interface WalletService {

    WalletResponse createWalletIfAbsent(UUID userId);

    WalletResponse topUp(TopUpRequest request);

    WalletResponse withdraw(WithdrawRequest request);

    WalletResponse payment(PaymentRequest request);

    WalletResponse paymentForOrder(UUID userId, BigDecimal amount, UUID orderId);

    WalletResponse refund(RefundRequest request);

    WalletResponse refundForOrder(UUID userId, BigDecimal amount, UUID orderId);
}
