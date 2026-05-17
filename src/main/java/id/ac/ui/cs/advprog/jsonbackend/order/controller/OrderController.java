package id.ac.ui.cs.advprog.jsonbackend.order.controller;

import id.ac.ui.cs.advprog.jsonbackend.order.dto.OrderRequest;
import id.ac.ui.cs.advprog.jsonbackend.order.dto.OrderResponse;
import id.ac.ui.cs.advprog.jsonbackend.order.dto.OrderStatusUpdateRequest;
import id.ac.ui.cs.advprog.jsonbackend.order.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
// Sesuai kesepakatan tadi, kita pakai singular "order"
@RequestMapping("/api/order")
public class OrderController {

    private final OrderService orderService;

    // Dependency Injection: Menyambungkan Controller ke Service
    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    // 1. Get All Orders (Untuk Dashboard)
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        List<OrderResponse> responses = orderService.findAll();
        return ResponseEntity.ok(responses);
    }

    // 2. Get Order by ID (Untuk Halaman Detail)
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable java.util.UUID id) {
        OrderResponse response = orderService.findById(id);
        return ResponseEntity.ok(response);
    }

    // 3. Create Order (Untuk Checkout)
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest request) {
        OrderResponse response = orderService.create(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // 4. Update Status (Untuk Jastiper update resi/progress)
    @PutMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateStatus(
            @PathVariable UUID id,
            @RequestBody OrderStatusUpdateRequest request) {
        OrderResponse response = orderService.updateStatus(id, request);
        return ResponseEntity.ok(response);
    }

    // 5. Get Orders by Titipers ID (Untuk riwayat Titiper)
    @GetMapping("/titipers/{titipersId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByTitipersId(@PathVariable UUID titipersId) {
        List<OrderResponse> responses = orderService.findByTitipersId(titipersId);
        return ResponseEntity.ok(responses);
    }

    // 6. Get Orders by Jastiper ID (Untuk to-do list Jastiper)
    @GetMapping("/jastiper/{jastiperId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByJastiperId(@PathVariable UUID jastiperId) {
        List<OrderResponse> responses = orderService.findByJastiperId(jastiperId);
        return ResponseEntity.ok(responses);
    }
}