package id.ac.ui.cs.advprog.jsonbackend.wallet.monitoring;

import id.ac.ui.cs.advprog.jsonbackend.wallet.model.TransactionStatus;
import id.ac.ui.cs.advprog.jsonbackend.wallet.model.TransactionType;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class WalletMetrics {

    private final MeterRegistry meterRegistry;

    public WalletMetrics(MeterRegistry meterRegistry) {
        this.meterRegistry = meterRegistry;
    }

    public void recordTransactionSuccess(TransactionType type, Duration duration) {
        Counter.builder("json.wallet.transaction.success")
                .description("Total successful wallet balance transactions")
                .tag("type", tagValue(type))
                .register(meterRegistry)
                .increment();
        recordTransactionDuration(type, "success", duration);
    }

    public void recordTransactionFailure(TransactionType type, Duration duration, Throwable cause) {
        Counter.builder("json.wallet.transaction.failure")
                .description("Total failed wallet balance transactions")
                .tag("type", tagValue(type))
                .tag("reason", reason(cause))
                .register(meterRegistry)
                .increment();
        recordTransactionDuration(type, "failure", duration);
    }

    public void recordPendingTransactionCreated(TransactionType type, Duration duration) {
        Counter.builder("json.wallet.transaction.pending")
                .description("Total pending wallet transactions created")
                .tag("type", tagValue(type))
                .register(meterRegistry)
                .increment();
        Timer.builder("json.wallet.transaction.pending.duration")
                .description("Pending wallet transaction creation duration")
                .tag("type", tagValue(type))
                .register(meterRegistry)
                .record(duration);
    }

    public void recordVerification(TransactionStatus status, Duration duration) {
        Counter.builder("json.wallet.transaction.verify")
                .description("Total wallet transaction verifications")
                .tag("status", tagValue(status))
                .register(meterRegistry)
                .increment();
        Timer.builder("json.wallet.transaction.verify.duration")
                .description("Wallet transaction verification duration")
                .register(meterRegistry)
                .record(duration);
    }

    private void recordTransactionDuration(TransactionType type, String outcome, Duration duration) {
        Timer.builder("json.wallet.transaction.duration")
                .description("Wallet balance transaction processing duration")
                .tag("type", tagValue(type))
                .tag("outcome", outcome)
                .register(meterRegistry)
                .record(duration);
    }

    private String tagValue(Enum<?> value) {
        return value == null ? "UNKNOWN" : value.name();
    }

    private String reason(Throwable cause) {
        if (cause == null) {
            return "Unknown";
        }
        return cause.getClass().getSimpleName();
    }
}
