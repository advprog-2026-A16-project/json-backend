package id.ac.ui.cs.advprog.jsonbackend.order.mapper;

import id.ac.ui.cs.advprog.jsonbackend.order.dto.OrderRequest;
import id.ac.ui.cs.advprog.jsonbackend.order.dto.OrderResponse;
import id.ac.ui.cs.advprog.jsonbackend.order.model.Order;
import id.ac.ui.cs.advprog.jsonbackend.order.model.OrderStatus;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class OrderMapperTest {

    private final OrderMapper orderMapper = new OrderMapper();

    @Test
    void toEntityReturnsNullWhenRequestIsNull() {
        assertNull(orderMapper.toEntity(null));
    }

    @Test
    void toEntityMapsRequestFields() {
        UUID titipersId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();

        OrderRequest request = OrderRequest.builder()
                .titipersId(titipersId)
                .productId(productId)
                .quantity(2)
                .shippingAddress("Jl. Margonda")
                .build();

        Order result = orderMapper.toEntity(request);

        assertEquals(titipersId, result.getTitipersId());
        assertEquals(productId, result.getProductId());
        assertEquals(2, result.getQuantity());
        assertEquals("Jl. Margonda", result.getShippingAddress());
        assertNull(result.getJastiperId());
        assertNull(result.getTotalPrice());
    }

    @Test
    void toResponseReturnsNullWhenOrderIsNull() {
        assertNull(orderMapper.toResponse(null));
    }

    @Test
    void toResponseMapsAllFields() {
        UUID orderId = UUID.randomUUID();
        UUID titipersId = UUID.randomUUID();
        UUID jastiperId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();

        Order order = Order.builder()
                .id(orderId)
                .titipersId(titipersId)
                .jastiperId(jastiperId)
                .productId(productId)
                .quantity(3)
                .shippingAddress("Depok")
                .totalPrice(new BigDecimal("75000"))
                .status(OrderStatus.PURCHASED)
                .jastiperRating(4)
                .productRating(5)
                .reviewNotes("Bagus")
                .createdAt(now)
                .updatedAt(now)
                .build();

        OrderResponse result = orderMapper.toResponse(order);

        assertEquals(orderId, result.getId());
        assertEquals(titipersId, result.getTitipersId());
        assertEquals(jastiperId, result.getJastiperId());
        assertEquals(productId, result.getProductId());
        assertEquals(3, result.getQuantity());
        assertEquals("Depok", result.getShippingAddress());
        assertEquals(new BigDecimal("75000"), result.getTotalPrice());
        assertEquals(OrderStatus.PURCHASED, result.getStatus());
        assertEquals(4, result.getJastiperRating());
        assertEquals(5, result.getProductRating());
        assertEquals("Bagus", result.getReviewNotes());
        assertEquals(now, result.getCreatedAt());
        assertEquals(now, result.getUpdatedAt());
    }
}
