package id.ac.ui.cs.advprog.jsonbackend.common.monitoring;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
public class ApplicationMetrics {

    private final Counter registerSuccessCounter;
    private final Timer registerTimer;
    private final Counter loginSuccessCounter;
    private final Counter loginFailureCounter;
    private final Timer loginTimer;
    private final Counter changePasswordSuccessCounter;
    private final Counter changePasswordFailureCounter;
    private final Timer changePasswordTimer;
    private final Counter orderCreateSuccessCounter;
    private final Counter orderCreateFailureCounter;
    private final Timer orderCreateTimer;
    private final Counter reserveStockSuccessCounter;
    private final Counter reserveStockFailureCounter;
    private final Timer reserveStockTimer;
    private final Counter kycApproveCounter;
    private final Timer kycApproveTimer;
    private final Counter kycRejectCounter;
    private final Timer kycRejectTimer;

    public ApplicationMetrics(MeterRegistry meterRegistry) {
        this.registerSuccessCounter = buildCounter(
                meterRegistry,
                "json.auth.register.success",
                "Total successful user registrations"
        );
        this.registerTimer = buildTimer(
                meterRegistry,
                "json.auth.register.duration",
                "Registration processing duration"
        );

        this.loginSuccessCounter = buildCounter(
                meterRegistry,
                "json.auth.login.success",
                "Total successful login attempts"
        );
        this.loginFailureCounter = buildCounter(
                meterRegistry,
                "json.auth.login.failure",
                "Total failed login attempts"
        );
        this.loginTimer = buildTimer(
                meterRegistry,
                "json.auth.login.duration",
                "Login processing duration"
        );

        this.changePasswordSuccessCounter = buildCounter(
                meterRegistry,
                "json.auth.change_password.success",
                "Total successful change password attempts"
        );
        this.changePasswordFailureCounter = buildCounter(
                meterRegistry,
                "json.auth.change_password.failure",
                "Total failed change password attempts"
        );
        this.changePasswordTimer = buildTimer(
                meterRegistry,
                "json.auth.change_password.duration",
                "Change password processing duration"
        );

        this.orderCreateSuccessCounter = buildCounter(
                meterRegistry,
                "json.order.create.success",
                "Total successful order creations"
        );
        this.orderCreateFailureCounter = buildCounter(
                meterRegistry,
                "json.order.create.failure",
                "Total failed order creations"
        );
        this.orderCreateTimer = buildTimer(
                meterRegistry,
                "json.order.create.duration",
                "Order creation processing duration"
        );

        this.reserveStockSuccessCounter = buildCounter(
                meterRegistry,
                "json.inventory.reserve_stock.success",
                "Total successful stock reservations"
        );
        this.reserveStockFailureCounter = buildCounter(
                meterRegistry,
                "json.inventory.reserve_stock.failure",
                "Total failed stock reservations"
        );
        this.reserveStockTimer = buildTimer(
                meterRegistry,
                "json.inventory.reserve_stock.duration",
                "Stock reservation processing duration"
        );

        this.kycApproveCounter = buildCounter(
                meterRegistry,
                "json.auth.kyc.approve",
                "Total approved KYC submissions"
        );
        this.kycApproveTimer = buildTimer(
                meterRegistry,
                "json.auth.kyc.approve.duration",
                "KYC approval processing duration"
        );

        this.kycRejectCounter = buildCounter(
                meterRegistry,
                "json.auth.kyc.reject",
                "Total rejected KYC submissions"
        );
        this.kycRejectTimer = buildTimer(
                meterRegistry,
                "json.auth.kyc.reject.duration",
                "KYC rejection processing duration"
        );
    }

    public void recordRegisterSuccess(Duration duration) {
        record(registerSuccessCounter, registerTimer, duration);
    }

    public void recordLoginSuccess(Duration duration) {
        record(loginSuccessCounter, loginTimer, duration);
    }

    public void recordLoginFailure(Duration duration) {
        record(loginFailureCounter, loginTimer, duration);
    }

    public void recordChangePasswordSuccess(Duration duration) {
        record(changePasswordSuccessCounter, changePasswordTimer, duration);
    }

    public void recordChangePasswordFailure(Duration duration) {
        record(changePasswordFailureCounter, changePasswordTimer, duration);
    }

    public void recordOrderCreateSuccess(Duration duration) {
        record(orderCreateSuccessCounter, orderCreateTimer, duration);
    }

    public void recordOrderCreateFailure(Duration duration) {
        record(orderCreateFailureCounter, orderCreateTimer, duration);
    }

    public void recordReserveStockSuccess(Duration duration) {
        record(reserveStockSuccessCounter, reserveStockTimer, duration);
    }

    public void recordReserveStockFailure(Duration duration) {
        record(reserveStockFailureCounter, reserveStockTimer, duration);
    }

    public void recordKycApprove(Duration duration) {
        record(kycApproveCounter, kycApproveTimer, duration);
    }

    public void recordKycReject(Duration duration) {
        record(kycRejectCounter, kycRejectTimer, duration);
    }

    private Counter buildCounter(MeterRegistry meterRegistry, String name, String description) {
        return Counter.builder(name)
                .description(description)
                .register(meterRegistry);
    }

    private Timer buildTimer(MeterRegistry meterRegistry, String name, String description) {
        return Timer.builder(name)
                .description(description)
                .register(meterRegistry);
    }

    private void record(Counter counter, Timer timer, Duration duration) {
        counter.increment();
        timer.record(duration);
    }
}
