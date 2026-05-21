package id.ac.ui.cs.advprog.jsonbackend.order.service;

import id.ac.ui.cs.advprog.jsonbackend.inventory.dto.ProductResponse;
import id.ac.ui.cs.advprog.jsonbackend.inventory.service.ProductService;
import id.ac.ui.cs.advprog.jsonbackend.common.monitoring.ApplicationMetrics;
import id.ac.ui.cs.advprog.jsonbackend.order.dto.OrderRatingRequest;
import id.ac.ui.cs.advprog.jsonbackend.order.dto.OrderRequest;
import id.ac.ui.cs.advprog.jsonbackend.order.dto.OrderResponse;
import id.ac.ui.cs.advprog.jsonbackend.order.dto.OrderStatusUpdateRequest;
import id.ac.ui.cs.advprog.jsonbackend.order.event.OrderEventPayloadFactory;
import id.ac.ui.cs.advprog.jsonbackend.order.event.OrderEventType;
import id.ac.ui.cs.advprog.jsonbackend.order.event.model.OrderOutboxEvent;
import id.ac.ui.cs.advprog.jsonbackend.order.event.repository.OrderOutboxEventRepository;
import id.ac.ui.cs.advprog.jsonbackend.order.exception.InvalidOrderException;
import id.ac.ui.cs.advprog.jsonbackend.order.exception.OrderNotFoundException;
import id.ac.ui.cs.advprog.jsonbackend.order.mapper.OrderMapper;
import id.ac.ui.cs.advprog.jsonbackend.order.model.Order;
import id.ac.ui.cs.advprog.jsonbackend.order.model.OrderStatus;
import id.ac.ui.cs.advprog.jsonbackend.order.repository.OrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderServiceImpl.class);

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final ProductService productService;
    private final OrderOutboxEventRepository outboxEventRepository;
    private final ApplicationMetrics applicationMetrics;

    public OrderServiceImpl(OrderRepository orderRepository,
                            OrderMapper orderMapper,
                            ProductService productService,
                            OrderOutboxEventRepository outboxEventRepository,
                            ApplicationMetrics applicationMetrics) {
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
        this.productService = productService;
        this.outboxEventRepository = outboxEventRepository;
        this.applicationMetrics = applicationMetrics;
    }

    @Override
    public OrderResponse create(OrderRequest request) {
        long startNanos = System.nanoTime();
        try {
            ProductResponse product = productService.findById(request.getProductId());

            if (product.getStock() < request.getQuantity()) {
                throw new InvalidOrderException("Stok tidak mencukupi untuk pesanan ini");
            }

            BigDecimal totalPrice = product.getPrice().multiply(BigDecimal.valueOf(request.getQuantity()));

            Order order = orderMapper.toEntity(request);
            order.setJastiperId(product.getJastiperId());
            order.setTotalPrice(totalPrice);
            order.setStatus(OrderStatus.PAID);

            Order savedOrder = orderRepository.save(order);

            // Mempublikasikan event checkout. Publisher akan reserve stock dulu,
            // lalu memicu pembayaran wallet jika reservasi berhasil.
            String orderCreatedPayload = OrderEventPayloadFactory.orderCreatedPayload(
                    savedOrder.getId(), savedOrder.getProductId(), savedOrder.getQuantity(),
                    savedOrder.getTitipersId(), savedOrder.getTotalPrice()
            );
            appendOutboxEvent(OrderEventType.ORDER_CREATED, savedOrder.getId(), orderCreatedPayload);

            log.info(
                    "Order event: CREATE_SUCCESS orderId={} productId={} titipersId={} quantity={} totalPrice={}",
                    savedOrder.getId(),
                    savedOrder.getProductId(),
                    savedOrder.getTitipersId(),
                    savedOrder.getQuantity(),
                    savedOrder.getTotalPrice()
            );
            applicationMetrics.recordOrderCreateSuccess(elapsed(startNanos));
            return orderMapper.toResponse(savedOrder);
        } catch (RuntimeException exception) {
            log.warn(
                    "Order event: CREATE_FAILURE reason={}",
                    exception.getClass().getSimpleName()
            );
            applicationMetrics.recordOrderCreateFailure(elapsed(startNanos));
            throw exception;
        }
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
        Order savedOrder = orderRepository.save(order);

        // Mempublikasikan event untuk Modul Wallet (Refund)
        String orderCancelledPayload = OrderEventPayloadFactory.orderCancelledPayload(
                savedOrder.getId(), savedOrder.getProductId(), savedOrder.getQuantity(),
                savedOrder.getTitipersId(), savedOrder.getTotalPrice()
        );
        appendOutboxEvent(OrderEventType.ORDER_CANCELLED, savedOrder.getId(), orderCancelledPayload);

        // Mempublikasikan event untuk Modul Inventory (Pelepasan Stok)
        String stockReleasePayload = OrderEventPayloadFactory.stockReleaseRequestedPayload(
                savedOrder.getId(), savedOrder.getProductId(), savedOrder.getQuantity()
        );
        appendOutboxEvent(OrderEventType.STOCK_RELEASE_REQUESTED, savedOrder.getId(), stockReleasePayload);

        return orderMapper.toResponse(savedOrder);
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

        Order savedOrder = orderRepository.save(order);

        String payload = OrderEventPayloadFactory.orderRatedPayload(
                savedOrder.getId(), savedOrder.getJastiperId(),
                savedOrder.getJastiperRating(), savedOrder.getProductRating()
        );
        appendOutboxEvent(OrderEventType.ORDER_RATED, savedOrder.getId(), payload);

        return orderMapper.toResponse(savedOrder);
    }

    private Order getOrderOrThrow(UUID id) {
        return orderRepository.findById(id).orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + id));
    }

    private void appendOutboxEvent(OrderEventType eventType, UUID aggregateId, String payload) {
        OrderOutboxEvent outboxEvent = OrderOutboxEvent.builder()
                .eventType(eventType)
                .aggregateId(aggregateId)
                .payload(payload)
                .correlationId(UUID.randomUUID().toString())
                .build();
        outboxEventRepository.save(outboxEvent);
    }

    private Duration elapsed(long startNanos) {
        return Duration.ofNanos(System.nanoTime() - startNanos);
    }
}
