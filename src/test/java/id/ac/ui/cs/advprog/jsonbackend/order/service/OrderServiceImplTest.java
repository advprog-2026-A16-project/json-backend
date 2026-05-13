package id.ac.ui.cs.advprog.jsonbackend.order.service;

import id.ac.ui.cs.advprog.jsonbackend.inventory.dto.ProductResponse;
import id.ac.ui.cs.advprog.jsonbackend.inventory.service.ProductService;
import id.ac.ui.cs.advprog.jsonbackend.order.dto.OrderRatingRequest;
import id.ac.ui.cs.advprog.jsonbackend.order.dto.OrderRequest;
import id.ac.ui.cs.advprog.jsonbackend.order.dto.OrderResponse;
import id.ac.ui.cs.advprog.jsonbackend.order.dto.OrderStatusUpdateRequest;
import id.ac.ui.cs.advprog.jsonbackend.order.event.model.OrderOutboxEvent;
import id.ac.ui.cs.advprog.jsonbackend.order.event.repository.OrderOutboxEventRepository;
import id.ac.ui.cs.advprog.jsonbackend.order.exception.InvalidOrderException;
import id.ac.ui.cs.advprog.jsonbackend.order.exception.OrderNotFoundException;
import id.ac.ui.cs.advprog.jsonbackend.order.mapper.OrderMapper;
import id.ac.ui.cs.advprog.jsonbackend.order.model.Order;
import id.ac.ui.cs.advprog.jsonbackend.order.model.OrderStatus;
import id.ac.ui.cs.advprog.jsonbackend.order.repository.OrderRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private OrderMapper orderMapper;

    @Mock
    private ProductService productService;

    @Mock
    private OrderOutboxEventRepository outboxEventRepository;

    @InjectMocks
    private OrderServiceImpl orderService;

    private UUID orderId, productId, titipersId, jastiperId;
    private Order order;
    private OrderResponse orderResponse;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        productId = UUID.randomUUID();
        titipersId = UUID.randomUUID();
        jastiperId = UUID.randomUUID();

        order = new Order();
        order.setId(orderId);
        order.setProductId(productId);
        order.setTitipersId(titipersId);
        order.setJastiperId(jastiperId);
        order.setQuantity(2);
        order.setTotalPrice(new BigDecimal("100000"));
        order.setStatus(OrderStatus.PAID);

        orderResponse = new OrderResponse();
        orderResponse.setId(orderId);
        orderResponse.setProductId(productId);
        orderResponse.setStatus(OrderStatus.PAID);
    }

    @Test
    void testCreateOrderEventDrivenSuccess() {
        OrderRequest request = new OrderRequest();
        request.setProductId(productId);
        request.setQuantity(2);
        request.setTitipersId(titipersId);

        ProductResponse product = new ProductResponse();
        product.setId(productId);
        product.setPrice(new BigDecimal("50000"));
        product.setStock(10);
        product.setJastiperId(jastiperId);

        when(productService.findById(productId)).thenReturn(product);
        when(orderMapper.toEntity(request)).thenReturn(order);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toResponse(order)).thenReturn(orderResponse);

        OrderResponse result = orderService.create(request);

        assertNotNull(result);

        verify(outboxEventRepository).save(any(OrderOutboxEvent.class));

        verify(productService, never()).reserveStock(any(), anyInt());
    }

    @Test
    void testCancelByJastiperEventDrivenSuccess() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toResponse(order)).thenReturn(orderResponse);

        OrderResponse result = orderService.cancelByJastiper(orderId);

        assertNotNull(result);
        assertEquals(OrderStatus.CANCELLED, order.getStatus());

        verify(outboxEventRepository).save(any(OrderOutboxEvent.class));
    }

    @Test
    void testGiveRatingEventDrivenSuccess() {
        order.setStatus(OrderStatus.COMPLETED);
        OrderRatingRequest request = new OrderRatingRequest();
        request.setJastiperRating(5);
        request.setProductRating(4);
        request.setReviewNotes("Bagus");

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toResponse(order)).thenReturn(orderResponse);

        OrderResponse result = orderService.giveRating(orderId, request);

        assertNotNull(result);


        verify(outboxEventRepository).save(any(OrderOutboxEvent.class));
    }
}