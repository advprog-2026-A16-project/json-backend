package id.ac.ui.cs.advprog.jsonbackend.order.model;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

class OrderTest {

    private UUID titipersId;
    private UUID jastiperId;
    private UUID productId;

    @BeforeEach
    void setUp() {
        this.titipersId = UUID.randomUUID();
        this.jastiperId = UUID.randomUUID();
        this.productId = UUID.randomUUID();
    }

    @Test
    void testOrderBuilderAndGetters() {
        Order order = Order.builder()
                .titipersId(titipersId)
                .jastiperId(jastiperId)
                .productId(productId)
                .quantity(2)
                .shippingAddress("Fasilkom UI, Depok")
                .totalPrice(new BigDecimal("150000"))
                .status(OrderStatus.PAID)
                .build();

        assertEquals(titipersId, order.getTitipersId());
        assertEquals(jastiperId, order.getJastiperId());
        assertEquals(productId, order.getProductId());
        assertEquals(2, order.getQuantity());
        assertEquals("Fasilkom UI, Depok", order.getShippingAddress());
        assertEquals(new BigDecimal("150000"), order.getTotalPrice());
        assertEquals(OrderStatus.PAID, order.getStatus());
    }

    @Test
    void testOnCreateSetsTimestamps() {
        Order order = new Order();
        order.onCreate();

        assertNotNull(order.getCreatedAt());
        assertNotNull(order.getUpdatedAt());
        assertEquals(order.getCreatedAt(), order.getUpdatedAt());
    }

    @Test
    void testOnUpdateChangesTimestamp() throws InterruptedException {
        Order order = new Order();
        order.onCreate();
        LocalDateTime firstUpdate = order.getUpdatedAt();

        Thread.sleep(10);

        order.onUpdate();

        assertNotNull(order.getUpdatedAt());
        assertTrue(order.getUpdatedAt().isAfter(firstUpdate));
    }

    @Test
    void testDefaultStatusIsPaid() {
        Order orderWithBuilder = Order.builder().build();
        assertEquals(OrderStatus.PAID, orderWithBuilder.getStatus());
    }
}