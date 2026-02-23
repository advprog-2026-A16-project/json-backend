package id.ac.ui.cs.advprog.jsonbackend.order.controller;

import id.ac.ui.cs.advprog.jsonbackend.order.model.Order;
import id.ac.ui.cs.advprog.jsonbackend.order.model.OrderStatus;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    @GetMapping
    public List<Order> getAllOrders() {
        return List.of(
                Order.builder()
                        .id("order-1")
                        .titipersId("titiper-001")
                        .jastiperId("jastiper-001")
                        .productId("1") // Relasi ke "Limited Edition Sneakers"
                        .quantity(1)
                        .shippingAddress("Jl. Margonda Raya, Depok")
                        .totalPrice(new BigDecimal("2500000"))
                        .status(OrderStatus.PURCHASED)
                        .createdAt(LocalDateTime.now().minusDays(1))
                        .updatedAt(LocalDateTime.now().minusDays(1))
                        .build(),
                Order.builder()
                        .id("order-2")
                        .titipersId("titiper-002")
                        .jastiperId("jastiper-002")
                        .productId("2") // Relasi ke "Korean Skincare Set"
                        .quantity(2)
                        .shippingAddress("Jl. Sudirman, Jakarta")
                        .totalPrice(new BigDecimal("1500000"))
                        .status(OrderStatus.SHIPPED)
                        .createdAt(LocalDateTime.now().minusDays(3))
                        .updatedAt(LocalDateTime.now().minusDays(1))
                        .build()
        );
    }

    @GetMapping("/{id}")
    public Order getOrderById(@PathVariable String id) {
        return Order.builder()
                .id(id)
                .titipersId("titiper-001")
                .jastiperId("jastiper-001")
                .productId("1")
                .quantity(1)
                .shippingAddress("Jl. Margonda Raya, Depok")
                .totalPrice(new BigDecimal("2500000"))
                .status(OrderStatus.PAID)
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
    }
}