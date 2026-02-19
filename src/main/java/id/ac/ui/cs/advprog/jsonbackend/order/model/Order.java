package id.ac.ui.cs.advprog.jsonbackend.order.model;

import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Order {
    private String id;

    // Relasi
    private String titipersId;
    private String jastiperId;
    private String productId;

    // Detail Checkout
    private Integer quantity;
    private String shippingAddress;
    private BigDecimal totalPrice;

    // Status
    @Builder.Default
    private OrderStatus status = OrderStatus.PAID;

    // Rating
    private Integer jastiperRating;
    private Integer productRating;
    private String reviewNotes;

    // Timestamp
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}