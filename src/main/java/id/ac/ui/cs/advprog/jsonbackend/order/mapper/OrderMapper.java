package id.ac.ui.cs.advprog.jsonbackend.order.mapper;

import id.ac.ui.cs.advprog.jsonbackend.order.dto.OrderRequest;
import id.ac.ui.cs.advprog.jsonbackend.order.dto.OrderResponse;
import id.ac.ui.cs.advprog.jsonbackend.order.model.Order;
import org.springframework.stereotype.Component;

@Component
public class OrderMapper {

    // Mengubah input DTO menjadi Entity (Database)
    public Order toEntity(OrderRequest request) {
        if (request == null) {
            return null;
        }

        return Order.builder()
                .titipersId(request.getTitipersId())
                .productId(request.getProductId())
                .quantity(request.getQuantity())
                .shippingAddress(request.getShippingAddress())
                // Catatan: jastiperId, totalPrice, dan status sengaja TIDAK di-map di sini.
                // Ketiga field tersebut bersifat dinamis/sensitif dan akan diisi langsung di dalam Service.
                .build();
    }

    // Mengubah Entity menjadi output DTO (Response API)
    public OrderResponse toResponse(Order order) {
        if (order == null) {
            return null;
        }

        return OrderResponse.builder()
                .id(order.getId())
                .titipersId(order.getTitipersId())
                .jastiperId(order.getJastiperId())
                .productId(order.getProductId())
                .quantity(order.getQuantity())
                .shippingAddress(order.getShippingAddress())
                .totalPrice(order.getTotalPrice())
                .status(order.getStatus())
                .jastiperRating(order.getJastiperRating())
                .productRating(order.getProductRating())
                .reviewNotes(order.getReviewNotes())
                .createdAt(order.getCreatedAt())
                .updatedAt(order.getUpdatedAt())
                .build();
    }
}