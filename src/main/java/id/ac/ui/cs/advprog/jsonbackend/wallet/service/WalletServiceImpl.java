package id.ac.ui.cs.advprog.jsonbackend.wallet.service;

import id.ac.ui.cs.advprog.jsonbackend.wallet.dto.*;
import id.ac.ui.cs.advprog.jsonbackend.wallet.model.*;
import id.ac.ui.cs.advprog.jsonbackend.wallet.repository.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import id.ac.ui.cs.advprog.jsonbackend.wallet.exception.*;

import java.math.BigDecimal;
import java.util.UUID;

@Service
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;

    public WalletServiceImpl(WalletRepository walletRepository,
                             TransactionRepository transactionRepository) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
    }

    private Wallet getWallet(UUID userId){
        validateUserId(userId);
        return walletRepository.findByUserIdForUpdate(userId)
                .orElseThrow(WalletNotFoundException::new);
    }

    @Override
    @Transactional
    public WalletResponse createWalletIfAbsent(UUID userId) {
        validateUserId(userId);
        return walletRepository.findByUserId(userId)
                .map(wallet -> new WalletResponse(wallet.getUserId(), wallet.getBalance()))
                .orElseGet(() -> {
                    Wallet wallet = new Wallet();
                    wallet.setUserId(userId);
                    Wallet savedWallet = walletRepository.save(wallet);
                    return new WalletResponse(savedWallet.getUserId(), savedWallet.getBalance());
                });
    }

    @Override
    @Transactional
    public WalletResponse topUp(TopUpRequest request){
        return applyTransaction(request.getUserId(), request.getAmount(), TransactionType.TOP_UP, null);
    }

    @Override
    @Transactional
    public WalletResponse withdraw(WithdrawRequest request){
        return applyTransaction(request.getUserId(), request.getAmount(), TransactionType.WITHDRAWAL, null);
    }

    @Override
    @Transactional
    public WalletResponse payment(PaymentRequest request) {
        return applyTransaction(request.getUserId(), request.getAmount(), TransactionType.PAYMENT, null);
    }

    @Override
    @Transactional(noRollbackFor = {InsufficientBalanceException.class, WalletNotFoundException.class})
    public WalletResponse paymentForOrder(UUID userId, BigDecimal amount, UUID orderId) {
        return applyTransaction(userId, amount, TransactionType.PAYMENT, orderId);
    }

    @Override
    @Transactional
    public WalletResponse refund(RefundRequest request) {
        return applyTransaction(request.getUserId(), request.getAmount(), TransactionType.REFUND, null);
    }

    @Override
    @Transactional
    public WalletResponse refundForOrder(UUID userId, BigDecimal amount, UUID orderId) {
        return applyTransaction(userId, amount, TransactionType.REFUND, orderId);
    }

    private WalletResponse applyTransaction(UUID userId, BigDecimal amount, TransactionType type, UUID referenceId) {
        validateUserId(userId);
        validateAmount(amount);

        if (referenceId != null) {
            var existingTransaction = transactionRepository.findByUserIdAndTypeAndReferenceId(userId, type, referenceId);
            if (existingTransaction.isPresent()) {
                Wallet wallet = getWallet(userId);
                return new WalletResponse(wallet.getUserId(), wallet.getBalance());
            }
        }

        Wallet wallet = getWallet(userId);

        if (type == TransactionType.TOP_UP || type == TransactionType.REFUND) {
            wallet.credit(amount);
        } else {
            wallet.debit(amount);
        }
        walletRepository.save(wallet);

        Transaction trx = new Transaction();
        trx.setUserId(userId);
        trx.setAmount(amount);
        trx.setType(type);
        trx.setReferenceId(referenceId);
        transactionRepository.save(trx);

        return new WalletResponse(wallet.getUserId(), wallet.getBalance());
    }

    private void validateUserId(UUID userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID is required");
        }
    }

    private void validateAmount(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Amount must be greater than zero");
        }
    }
}
