package id.ac.ui.cs.advprog.jsonbackend.order.dto;

import id.ac.ui.cs.advprog.jsonbackend.order.model.OrderStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {
    private UUID id;

    // Relasi
    private UUID titipersId;
    private UUID jastiperId;
    private UUID productId;

    // Detail Checkout
    private Integer quantity;
    private String shippingAddress;
    private BigDecimal totalPrice;

    // Status
    private OrderStatus status;

    // Rating (Bisa null jika status belum COMPLETED)
    private Integer jastiperRating;
    private Integer productRating;
    private String reviewNotes;

    // Timestamp
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}