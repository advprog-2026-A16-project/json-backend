package id.ac.ui.cs.advprog.jsonbackend.wallet.monitoring;

import id.ac.ui.cs.advprog.jsonbackend.wallet.exception.InsufficientBalanceException;
import id.ac.ui.cs.advprog.jsonbackend.wallet.model.TransactionStatus;
import id.ac.ui.cs.advprog.jsonbackend.wallet.model.TransactionType;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WalletMetricsTest {

    private SimpleMeterRegistry meterRegistry;
    private WalletMetrics walletMetrics;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        walletMetrics = new WalletMetrics(meterRegistry);
    }

    @Test
    void transactionMetricsShouldRecordSuccessFailureAndDuration() {
        walletMetrics.recordTransactionSuccess(TransactionType.TOP_UP, Duration.ofMillis(10));
        walletMetrics.recordTransactionFailure(
                TransactionType.PAYMENT,
                Duration.ofMillis(15),
                new InsufficientBalanceException()
        );

        assertEquals(1.0, meterRegistry.get("json.wallet.transaction.success")
                .tag("type", "TOP_UP")
                .counter()
                .count());
        assertEquals(1.0, meterRegistry.get("json.wallet.transaction.failure")
                .tag("type", "PAYMENT")
                .tag("reason", "InsufficientBalanceException")
                .counter()
                .count());
        assertEquals(1L, meterRegistry.get("json.wallet.transaction.duration")
                .tag("type", "TOP_UP")
                .tag("outcome", "success")
                .timer()
                .count());
        assertEquals(1L, meterRegistry.get("json.wallet.transaction.duration")
                .tag("type", "PAYMENT")
                .tag("outcome", "failure")
                .timer()
                .count());
    }

    @Test
    void pendingAndVerificationMetricsShouldRecordCountersAndTimers() {
        walletMetrics.recordPendingTransactionCreated(TransactionType.WITHDRAWAL, Duration.ofMillis(12));
        walletMetrics.recordVerification(TransactionStatus.SUCCESS, Duration.ofMillis(20));
        walletMetrics.recordVerification(TransactionStatus.FAILED, Duration.ofMillis(25));

        assertEquals(1.0, meterRegistry.get("json.wallet.transaction.pending")
                .tag("type", "WITHDRAWAL")
                .counter()
                .count());
        assertEquals(1.0, meterRegistry.get("json.wallet.transaction.verify")
                .tag("status", "SUCCESS")
                .counter()
                .count());
        assertEquals(1.0, meterRegistry.get("json.wallet.transaction.verify")
                .tag("status", "FAILED")
                .counter()
                .count());
        assertEquals(1L, meterRegistry.get("json.wallet.transaction.pending.duration")
                .tag("type", "WITHDRAWAL")
                .timer()
                .count());
        assertEquals(2L, meterRegistry.get("json.wallet.transaction.verify.duration")
                .timer()
                .count());
    }
}
