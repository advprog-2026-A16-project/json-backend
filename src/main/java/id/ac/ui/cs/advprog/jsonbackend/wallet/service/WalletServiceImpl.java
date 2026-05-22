package id.ac.ui.cs.advprog.jsonbackend.wallet.service;

import id.ac.ui.cs.advprog.jsonbackend.wallet.dto.*;
import id.ac.ui.cs.advprog.jsonbackend.wallet.model.*;
import id.ac.ui.cs.advprog.jsonbackend.wallet.monitoring.WalletMetrics;
import id.ac.ui.cs.advprog.jsonbackend.wallet.payment.MidtransOrderId;
import id.ac.ui.cs.advprog.jsonbackend.wallet.payment.PaymentGateway;
import id.ac.ui.cs.advprog.jsonbackend.wallet.payment.PaymentGatewayChargeRequest;
import id.ac.ui.cs.advprog.jsonbackend.wallet.payment.PaymentGatewayChargeResponse;
import id.ac.ui.cs.advprog.jsonbackend.wallet.repository.*;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import id.ac.ui.cs.advprog.jsonbackend.wallet.exception.*;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

@Service
public class WalletServiceImpl implements WalletService {

    private final WalletRepository walletRepository;
    private final TransactionRepository transactionRepository;
    private final PaymentGateway paymentGateway;
    private final WalletMetrics walletMetrics;

    public WalletServiceImpl(WalletRepository walletRepository,
                             TransactionRepository transactionRepository,
                             PaymentGateway paymentGateway,
                             WalletMetrics walletMetrics) {
        this.walletRepository = walletRepository;
        this.transactionRepository = transactionRepository;
        this.paymentGateway = paymentGateway;
        this.walletMetrics = walletMetrics;
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
        return recordTransaction(
                TransactionType.TOP_UP,
                () -> applyTransaction(request.getUserId(), request.getAmount(), TransactionType.TOP_UP, null)
        );
    }

    @Override
    @Transactional
    public WalletResponse withdraw(WithdrawRequest request){
        return recordTransaction(TransactionType.WITHDRAWAL, () -> {
            validateDestinationAccount(request.getDestinationAccount());
            return applyTransaction(request.getUserId(), request.getAmount(), TransactionType.WITHDRAWAL, null);
        });
    }

    @Override
    @Transactional
    public TransactionResponse requestTopUp(TopUpRequest request) {
        return recordPendingTransaction(TransactionType.TOP_UP, () -> {
            validateUserId(request.getUserId());
            validateAmount(request.getAmount());
            getWallet(request.getUserId());

            Transaction transaction = new Transaction();
            transaction.setUserId(request.getUserId());
            transaction.setAmount(request.getAmount());
            transaction.setType(TransactionType.TOP_UP);
            transaction.setStatus(TransactionStatus.PENDING);
            transaction.setDescription("Top-up pending verification");

            return TransactionResponse.from(transactionRepository.save(transaction));
        });
    }

    @Override
    @Transactional
    public PaymentGatewayTopUpResponse requestTopUpPayment(TopUpRequest request) {
        return recordPendingTransaction(TransactionType.TOP_UP, () -> {
            validateUserId(request.getUserId());
            validateAmount(request.getAmount());
            getWallet(request.getUserId());

            Transaction transaction = new Transaction();
            transaction.setUserId(request.getUserId());
            transaction.setAmount(request.getAmount());
            transaction.setType(TransactionType.TOP_UP);
            transaction.setStatus(TransactionStatus.PENDING);
            transaction.setDescription("Top-up pending Midtrans payment");

            transaction = transactionRepository.save(transaction);
            if (transaction.getId() == null) {
                throw new IllegalStateException("Transaction ID was not generated");
            }

            String gatewayOrderId = MidtransOrderId.forTopUp(transaction.getId());
            PaymentGatewayChargeResponse chargeResponse = paymentGateway.createCharge(
                    new PaymentGatewayChargeRequest(
                            gatewayOrderId,
                            request.getAmount(),
                            request.getUserId(),
                            "Wallet Top-up"
                    )
            );

            transaction.setPaymentProvider("MIDTRANS");
            transaction.setGatewayOrderId(gatewayOrderId);
            transaction.setPaymentToken(chargeResponse.token());
            transaction.setPaymentRedirectUrl(chargeResponse.redirectUrl());

            return PaymentGatewayTopUpResponse.from(transactionRepository.save(transaction));
        });
    }

    @Override
    @Transactional
    public TransactionResponse requestWithdrawal(WithdrawRequest request) {
        return recordPendingTransaction(TransactionType.WITHDRAWAL, () -> {
            validateUserId(request.getUserId());
            validateAmount(request.getAmount());
            validateDestinationAccount(request.getDestinationAccount());

            Wallet wallet = getWallet(request.getUserId());
            if (wallet.getBalance().compareTo(request.getAmount()) < 0) {
                throw new InsufficientBalanceException();
            }

            Transaction transaction = new Transaction();
            transaction.setUserId(request.getUserId());
            transaction.setAmount(request.getAmount());
            transaction.setType(TransactionType.WITHDRAWAL);
            transaction.setStatus(TransactionStatus.PENDING);
            transaction.setDestinationAccount(request.getDestinationAccount().trim());
            transaction.setDescription("Withdrawal pending verification");

            return TransactionResponse.from(transactionRepository.save(transaction));
        });
    }

    @Override
    @Transactional
    public WalletResponse payment(PaymentRequest request) {
        return recordTransaction(
                TransactionType.PAYMENT,
                () -> applyTransaction(request.getUserId(), request.getAmount(), TransactionType.PAYMENT, null)
        );
    }

    @Override
    @Transactional(noRollbackFor = {InsufficientBalanceException.class, WalletNotFoundException.class})
    public WalletResponse paymentForOrder(UUID userId, BigDecimal amount, UUID orderId) {
        return recordTransaction(
                TransactionType.PAYMENT,
                () -> applyTransaction(userId, amount, TransactionType.PAYMENT, orderId)
        );
    }

    @Override
    @Transactional
    public WalletResponse refund(RefundRequest request) {
        return recordTransaction(
                TransactionType.REFUND,
                () -> applyTransaction(request.getUserId(), request.getAmount(), TransactionType.REFUND, null)
        );
    }

    @Override
    @Transactional
    public WalletResponse refundForOrder(UUID userId, BigDecimal amount, UUID orderId) {
        return recordTransaction(
                TransactionType.REFUND,
                () -> applyTransaction(userId, amount, TransactionType.REFUND, orderId)
        );
    }

    @Override
    @Transactional
    public TransactionResponse verifyTransaction(UUID transactionId, VerifyTransactionRequest request) {
        long startNanos = System.nanoTime();
        if (transactionId == null) {
            throw new IllegalArgumentException("Transaction ID is required");
        }
        if (request == null || request.getSuccess() == null) {
            throw new IllegalArgumentException("Verification result is required");
        }

        Transaction transaction = transactionRepository.findById(transactionId)
                .orElseThrow(() -> new IllegalArgumentException("Transaction not found"));

        if (transaction.getStatus() != TransactionStatus.PENDING) {
            return TransactionResponse.from(transaction);
        }

        if (Boolean.TRUE.equals(request.getSuccess())) {
            Wallet wallet = getWallet(transaction.getUserId());
            applyVerifiedMutation(wallet, transaction);
            walletRepository.save(wallet);
            transaction.setStatus(TransactionStatus.SUCCESS);
        } else {
            transaction.setStatus(TransactionStatus.FAILED);
        }

        if (request.getDescription() != null && !request.getDescription().isBlank()) {
            transaction.setDescription(request.getDescription().trim());
        }
        transaction.setVerifiedAt(LocalDateTime.now());

        TransactionResponse response = TransactionResponse.from(transactionRepository.save(transaction));
        walletMetrics.recordVerification(response.getStatus(), elapsed(startNanos));
        return response;
    }

    @Override
    @Transactional(readOnly = true)
    public List<TransactionResponse> getTransactionHistory(UUID userId) {
        validateUserId(userId);
        return transactionRepository.findByUserIdOrderByCreatedAtDesc(userId)
                .stream()
                .map(TransactionResponse::from)
                .toList();
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
        trx.setStatus(TransactionStatus.SUCCESS);
        trx.setDescription(defaultDescription(type));
        transactionRepository.save(trx);

        return new WalletResponse(wallet.getUserId(), wallet.getBalance());
    }

    private WalletResponse recordTransaction(TransactionType type, Supplier<WalletResponse> action) {
        long startNanos = System.nanoTime();
        try {
            WalletResponse response = action.get();
            walletMetrics.recordTransactionSuccess(type, elapsed(startNanos));
            return response;
        } catch (RuntimeException exception) {
            walletMetrics.recordTransactionFailure(type, elapsed(startNanos), exception);
            throw exception;
        }
    }

    private <T> T recordPendingTransaction(TransactionType type, Supplier<T> action) {
        long startNanos = System.nanoTime();
        try {
            T response = action.get();
            walletMetrics.recordPendingTransactionCreated(type, elapsed(startNanos));
            return response;
        } catch (RuntimeException exception) {
            walletMetrics.recordTransactionFailure(type, elapsed(startNanos), exception);
            throw exception;
        }
    }

    private Duration elapsed(long startNanos) {
        return Duration.ofNanos(System.nanoTime() - startNanos);
    }

    private void applyVerifiedMutation(Wallet wallet, Transaction transaction) {
        if (transaction.getType() == TransactionType.TOP_UP || transaction.getType() == TransactionType.REFUND) {
            wallet.credit(transaction.getAmount());
        } else {
            wallet.debit(transaction.getAmount());
        }
    }

    private String defaultDescription(TransactionType type) {
        return switch (type) {
            case TOP_UP -> "Top-up balance";
            case WITHDRAWAL -> "Withdrawal balance";
            case PAYMENT -> "Payment for order";
            case REFUND -> "Refund balance";
        };
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

    private void validateDestinationAccount(String destinationAccount) {
        if (destinationAccount == null || destinationAccount.isBlank()) {
            throw new IllegalArgumentException("Destination account is required");
        }
    }
}
