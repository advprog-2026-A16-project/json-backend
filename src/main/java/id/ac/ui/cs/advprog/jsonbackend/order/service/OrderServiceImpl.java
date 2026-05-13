package id.ac.ui.cs.advprog.jsonbackend.order.service;

import id.ac.ui.cs.advprog.jsonbackend.inventory.dto.ProductResponse;
import id.ac.ui.cs.advprog.jsonbackend.inventory.service.ProductService;
import id.ac.ui.cs.advprog.jsonbackend.order.dto.OrderRatingRequest;
import id.ac.ui.cs.advprog.jsonbackend.order.dto.OrderRequest;
import id.ac.ui.cs.advprog.jsonbackend.order.dto.OrderResponse;
import id.ac.ui.cs.advprog.jsonbackend.order.dto.OrderStatusUpdateRequest;
import id.ac.ui.cs.advprog.jsonbackend.order.exception.InvalidOrderException;
import id.ac.ui.cs.advprog.jsonbackend.order.exception.OrderNotFoundException;
import id.ac.ui.cs.advprog.jsonbackend.order.mapper.OrderMapper;
import id.ac.ui.cs.advprog.jsonbackend.order.model.Order;
import id.ac.ui.cs.advprog.jsonbackend.order.model.OrderStatus;
import id.ac.ui.cs.advprog.jsonbackend.order.repository.OrderRepository;
import id.ac.ui.cs.advprog.jsonbackend.wallet.dto.PaymentRequest;
import id.ac.ui.cs.advprog.jsonbackend.wallet.dto.RefundRequest;
import id.ac.ui.cs.advprog.jsonbackend.wallet.service.WalletService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final ProductService productService;
    private final WalletService walletService;

    public OrderServiceImpl(OrderRepository orderRepository, OrderMapper orderMapper,
                            ProductService productService, WalletService walletService) {
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
        this.productService = productService;
        this.walletService = walletService;
    }

    @Override
    public OrderResponse create(OrderRequest request) {
        ProductResponse product = productService.findById(request.getProductId());

        if (product.getStock() < request.getQuantity()) {
            throw new InvalidOrderException("Stok tidak mencukupi untuk pesanan ini");
        }

        BigDecimal totalPrice = product.getPrice().multiply(BigDecimal.valueOf(request.getQuantity()));

        // (Wallet) Potong saldo secara Synchronous
        PaymentRequest paymentRequest = new PaymentRequest();
        paymentRequest.setUserId(request.getTitipersId());
        paymentRequest.setAmount(totalPrice);
        walletService.payment(paymentRequest);

        // (Inventory) Kurang stok secara Synchronous
        productService.reserveStock(product.getId(), request.getQuantity());

        Order order = orderMapper.toEntity(request);
        order.setJastiperId(product.getJastiperId());
        order.setTotalPrice(totalPrice);
        order.setStatus(OrderStatus.PAID);

        Order saved = orderRepository.save(order);
        return orderMapper.toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> findAll() {
        return orderRepository.findAll().stream().map(orderMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public OrderResponse findById(UUID id) {
        return orderMapper.toResponse(getOrderOrThrow(id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> findByTitipersId(UUID titipersId) {
        return orderRepository.findByTitipersId(titipersId).stream().map(orderMapper::toResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> findByJastiperId(UUID jastiperId) {
        return orderRepository.findByJastiperId(jastiperId).stream().map(orderMapper::toResponse).toList();
    }

    @Override
    public OrderResponse updateStatus(UUID id, OrderStatusUpdateRequest request) {
        Order order = getOrderOrThrow(id);
        OrderStatus newStatus = request.getNewStatus();

        if (newStatus == null) {
            throw new InvalidOrderException("Status pesanan tidak boleh kosong");
        }

        validateStatusTransition(order.getStatus(), newStatus);
        order.setStatus(newStatus);

        return orderMapper.toResponse(orderRepository.save(order));
    }

    private void validateStatusTransition(OrderStatus currentStatus, OrderStatus newStatus) {
        boolean isValid = switch (currentStatus) {
            case PAID -> newStatus == OrderStatus.PURCHASED;
            case PURCHASED -> newStatus == OrderStatus.SHIPPED;
            case SHIPPED -> newStatus == OrderStatus.COMPLETED;
            default -> false;
        };

        if (!isValid) {
            throw new InvalidOrderException("Transisi status tidak valid");
        }
    }

    @Override
    public OrderResponse cancelByJastiper(UUID id) {
        Order order = getOrderOrThrow(id);

        if (order.getStatus() == OrderStatus.SHIPPED || order.getStatus() == OrderStatus.COMPLETED) {
            throw new InvalidOrderException("Tidak bisa membatalkan pesanan yang sudah dikirim atau selesai.");
        }

        order.setStatus(OrderStatus.CANCELLED);
        Order saved = orderRepository.save(order);

        // (Wallet) Refund
        RefundRequest refundRequest = new RefundRequest();
        refundRequest.setUserId(order.getTitipersId());
        refundRequest.setAmount(order.getTotalPrice());
        walletService.refund(refundRequest);

        // (Inventory) Release Stok
        productService.releaseStock(order.getProductId(), order.getQuantity());

        return orderMapper.toResponse(saved);
    }

    @Override
    public OrderResponse giveRating(UUID id, OrderRatingRequest request) {
        Order order = getOrderOrThrow(id);

        if (order.getStatus() != OrderStatus.COMPLETED) {
            throw new InvalidOrderException("Hanya pesanan dengan status COMPLETED yang dapat diberi rating.");
        }
        if (order.getJastiperRating() != null || order.getProductRating() != null) {
            throw new InvalidOrderException("Pesanan ini sudah diberi rating sebelumnya.");
        }

        order.setJastiperRating(request.getJastiperRating());
        order.setProductRating(request.getProductRating());
        if (request.getReviewNotes() != null) {
            order.setReviewNotes(request.getReviewNotes());
        }

        return orderMapper.toResponse(orderRepository.save(order));
    }

    private Order getOrderOrThrow(UUID id) {
        return orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + id));
    }
}