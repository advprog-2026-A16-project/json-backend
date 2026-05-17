package id.ac.ui.cs.advprog.jsonbackend.order.service;

import id.ac.ui.cs.advprog.jsonbackend.order.dto.OrderRatingRequest;
import id.ac.ui.cs.advprog.jsonbackend.order.dto.OrderRequest;
import id.ac.ui.cs.advprog.jsonbackend.order.dto.OrderResponse;
import id.ac.ui.cs.advprog.jsonbackend.order.dto.OrderStatusUpdateRequest;

import java.util.List;
import java.util.UUID;

public interface OrderService {

    // Monitoring
    List<OrderResponse> findAll();
    OrderResponse findById(UUID id);
    List<OrderResponse> findByTitipersId(UUID titipersId);
    List<OrderResponse> findByJastiperId(UUID jastiperId);

    // Checkout
    OrderResponse create(OrderRequest request);

    // Status & Pembatalan
    OrderResponse updateStatus(UUID id, OrderStatusUpdateRequest request);
    OrderResponse cancelByJastiper(UUID id);

    // Rating
    OrderResponse giveRating(UUID id, OrderRatingRequest request);
}