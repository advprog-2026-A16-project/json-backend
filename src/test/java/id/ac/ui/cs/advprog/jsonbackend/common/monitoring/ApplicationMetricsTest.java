package id.ac.ui.cs.advprog.jsonbackend.common.monitoring;

import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ApplicationMetricsTest {

    private SimpleMeterRegistry meterRegistry;
    private ApplicationMetrics applicationMetrics;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
        applicationMetrics = new ApplicationMetrics(meterRegistry);
    }

    @Test
    void authMetricsShouldIncrementCountersAndTimers() {
        applicationMetrics.recordRegisterSuccess(Duration.ofMillis(10));
        applicationMetrics.recordLoginSuccess(Duration.ofMillis(15));
        applicationMetrics.recordLoginFailure(Duration.ofMillis(20));
        applicationMetrics.recordChangePasswordSuccess(Duration.ofMillis(10));
        applicationMetrics.recordChangePasswordFailure(Duration.ofMillis(15));


        assertEquals(1.0, meterRegistry.get("json.auth.register.success").counter().count());
        assertEquals(1.0, meterRegistry.get("json.auth.login.success").counter().count());
        assertEquals(1.0, meterRegistry.get("json.auth.login.failure").counter().count());
        assertEquals(1.0, meterRegistry.get("json.auth.change_password.success").counter().count());
        assertEquals(1.0, meterRegistry.get("json.auth.change_password.failure").counter().count());
        assertEquals(1L, meterRegistry.get("json.auth.register.duration").timer().count());
        assertEquals(2L, meterRegistry.get("json.auth.login.duration").timer().count());
        assertEquals(2L, meterRegistry.get("json.auth.change_password.duration").timer().count());
    }

    @Test
    void orderAndInventoryMetricsShouldIncrementCountersAndTimers() {
        applicationMetrics.recordOrderCreateSuccess(Duration.ofMillis(25));
        applicationMetrics.recordOrderCreateFailure(Duration.ofMillis(30));
        applicationMetrics.recordReserveStockSuccess(Duration.ofMillis(35));
        applicationMetrics.recordReserveStockFailure(Duration.ofMillis(40));

        assertEquals(1.0, meterRegistry.get("json.order.create.success").counter().count());
        assertEquals(1.0, meterRegistry.get("json.order.create.failure").counter().count());
        assertEquals(1.0, meterRegistry.get("json.inventory.reserve_stock.success").counter().count());
        assertEquals(1.0, meterRegistry.get("json.inventory.reserve_stock.failure").counter().count());
        assertEquals(2L, meterRegistry.get("json.order.create.duration").timer().count());
        assertEquals(2L, meterRegistry.get("json.inventory.reserve_stock.duration").timer().count());
    }

    @Test
    void kycMetricsShouldIncrementCountersAndTimers() {
        applicationMetrics.recordKycApprove(Duration.ofMillis(18));
        applicationMetrics.recordKycReject(Duration.ofMillis(22));

        assertEquals(1.0, meterRegistry.get("json.auth.kyc.approve").counter().count());
        assertEquals(1.0, meterRegistry.get("json.auth.kyc.reject").counter().count());
        assertEquals(1L, meterRegistry.get("json.auth.kyc.approve.duration").timer().count());
        assertEquals(1L, meterRegistry.get("json.auth.kyc.reject.duration").timer().count());
    }
}
