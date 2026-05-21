package id.ac.ui.cs.advprog.jsonbackend.order.service;

import id.ac.ui.cs.advprog.jsonbackend.inventory.dto.ProductResponse;
import id.ac.ui.cs.advprog.jsonbackend.inventory.service.ProductService;
import id.ac.ui.cs.advprog.jsonbackend.common.monitoring.ApplicationMetrics;
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

import java.time.Duration;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

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

    @Mock
    private ApplicationMetrics applicationMetrics;

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
        request.setShippingAddress("Fasilkom UI, Depok");

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
        verify(orderRepository).save(argThat(savedOrder ->
                savedOrder.getJastiperId().equals(jastiperId)
                        && savedOrder.getTotalPrice().compareTo(new BigDecimal("100000")) == 0
                        && savedOrder.getStatus() == OrderStatus.PAID
        ));

        org.mockito.ArgumentCaptor<OrderOutboxEvent> captor = org.mockito.ArgumentCaptor.forClass(OrderOutboxEvent.class);
        verify(outboxEventRepository).save(captor.capture());
        List<String> eventTypes = captor.getAllValues().stream()
                .map(event -> event.getEventType().name())
                .collect(Collectors.toList());
        assertTrue(eventTypes.contains("ORDER_CREATED"));

        verify(productService, never()).reserveStock(any(), anyInt());
        verify(applicationMetrics).recordOrderCreateSuccess(any(Duration.class));
    }

    @Test
    void testCreateOrderRecordsFailureMetricWhenStockIsInsufficient() {
        OrderRequest request = new OrderRequest();
        request.setProductId(productId);
        request.setQuantity(99);
        request.setTitipersId(titipersId);

        ProductResponse product = new ProductResponse();
        product.setId(productId);
        product.setPrice(new BigDecimal("50000"));
        product.setStock(1);
        product.setJastiperId(jastiperId);

        when(productService.findById(productId)).thenReturn(product);

        assertThrows(InvalidOrderException.class, () -> orderService.create(request));

        verify(applicationMetrics).recordOrderCreateFailure(any(Duration.class));
        verifyNoInteractions(outboxEventRepository);
    }

    @Test
    void testCreateOrderRecordsFailureMetricWhenProductLookupFails() {
        OrderRequest request = new OrderRequest();
        request.setProductId(productId);
        request.setQuantity(1);
        request.setTitipersId(titipersId);

        when(productService.findById(productId)).thenThrow(new RuntimeException("inventory unavailable"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> orderService.create(request));

        assertEquals("inventory unavailable", exception.getMessage());
        verify(applicationMetrics).recordOrderCreateFailure(any(Duration.class));
        verifyNoInteractions(outboxEventRepository);
    }

    @Test
    void testFindAllReturnsMappedResponses() {
        Order secondOrder = new Order();
        secondOrder.setId(UUID.randomUUID());
        OrderResponse secondResponse = new OrderResponse();
        secondResponse.setId(secondOrder.getId());

        when(orderRepository.findAll()).thenReturn(List.of(order, secondOrder));
        when(orderMapper.toResponse(order)).thenReturn(orderResponse);
        when(orderMapper.toResponse(secondOrder)).thenReturn(secondResponse);

        List<OrderResponse> result = orderService.findAll();

        assertEquals(2, result.size());
        assertEquals(orderId, result.get(0).getId());
        assertEquals(secondOrder.getId(), result.get(1).getId());
    }

    @Test
    void testFindByIdReturnsMappedResponse() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderMapper.toResponse(order)).thenReturn(orderResponse);

        OrderResponse result = orderService.findById(orderId);

        assertEquals(orderId, result.getId());
    }

    @Test
    void testFindByIdThrowsWhenOrderMissing() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        OrderNotFoundException exception = assertThrows(OrderNotFoundException.class, () -> orderService.findById(orderId));

        assertTrue(exception.getMessage().contains(orderId.toString()));
    }

    @Test
    void testFindByTitipersIdReturnsMappedResponses() {
        when(orderRepository.findByTitipersId(titipersId)).thenReturn(List.of(order));
        when(orderMapper.toResponse(order)).thenReturn(orderResponse);

        List<OrderResponse> result = orderService.findByTitipersId(titipersId);

        assertEquals(1, result.size());
        assertEquals(orderId, result.getFirst().getId());
    }

    @Test
    void testFindByJastiperIdReturnsMappedResponses() {
        when(orderRepository.findByJastiperId(jastiperId)).thenReturn(List.of(order));
        when(orderMapper.toResponse(order)).thenReturn(orderResponse);

        List<OrderResponse> result = orderService.findByJastiperId(jastiperId);

        assertEquals(1, result.size());
        assertEquals(orderId, result.getFirst().getId());
    }

    @Test
    void testUpdateStatusSuccess() {
        OrderStatusUpdateRequest request = new OrderStatusUpdateRequest(OrderStatus.PURCHASED);
        order.setStatus(OrderStatus.PAID);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(order)).thenReturn(order);
        when(orderMapper.toResponse(order)).thenReturn(orderResponse);

        OrderResponse result = orderService.updateStatus(orderId, request);

        assertEquals(orderResponse, result);
        assertEquals(OrderStatus.PURCHASED, order.getStatus());
        verify(orderRepository).save(order);
    }

    @Test
    void testUpdateStatusThrowsWhenNewStatusNull() {
        OrderStatusUpdateRequest request = new OrderStatusUpdateRequest(null);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        InvalidOrderException exception = assertThrows(InvalidOrderException.class,
                () -> orderService.updateStatus(orderId, request));

        assertEquals("Status pesanan tidak boleh kosong", exception.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testUpdateStatusThrowsWhenTransitionInvalid() {
        OrderStatusUpdateRequest request = new OrderStatusUpdateRequest(OrderStatus.COMPLETED);
        order.setStatus(OrderStatus.PAID);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        InvalidOrderException exception = assertThrows(InvalidOrderException.class,
                () -> orderService.updateStatus(orderId, request));

        assertEquals("Transisi status tidak valid", exception.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testUpdateStatusThrowsWhenOrderMissing() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class,
                () -> orderService.updateStatus(orderId, new OrderStatusUpdateRequest(OrderStatus.PURCHASED)));
    }

    @Test
    void testCancelByJastiperEventDrivenSuccess() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toResponse(order)).thenReturn(orderResponse);

        OrderResponse result = orderService.cancelByJastiper(orderId);

        assertNotNull(result);
        assertEquals(OrderStatus.CANCELLED, order.getStatus());

        verify(outboxEventRepository, times(2)).save(any(OrderOutboxEvent.class));
    }

    @Test
    void testCancelByJastiperThrowsWhenOrderAlreadyShipped() {
        order.setStatus(OrderStatus.SHIPPED);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        InvalidOrderException exception = assertThrows(InvalidOrderException.class,
                () -> orderService.cancelByJastiper(orderId));

        assertEquals("Tidak bisa membatalkan pesanan yang sudah dikirim atau selesai.", exception.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
        verifyNoInteractions(outboxEventRepository);
    }

    @Test
    void testCancelByJastiperThrowsWhenOrderMissing() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> orderService.cancelByJastiper(orderId));
        verifyNoInteractions(outboxEventRepository);
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

    @Test
    void testGiveRatingSuccessWithoutReviewNotes() {
        order.setStatus(OrderStatus.COMPLETED);
        OrderRatingRequest request = new OrderRatingRequest();
        request.setJastiperRating(4);
        request.setProductRating(5);
        request.setReviewNotes(null);

        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toResponse(order)).thenReturn(orderResponse);

        OrderResponse result = orderService.giveRating(orderId, request);

        assertNotNull(result);
        assertNull(order.getReviewNotes());
        verify(outboxEventRepository).save(any(OrderOutboxEvent.class));
    }

    @Test
    void testGiveRatingThrowsWhenOrderNotCompleted() {
        order.setStatus(OrderStatus.PAID);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        InvalidOrderException exception = assertThrows(InvalidOrderException.class,
                () -> orderService.giveRating(orderId, new OrderRatingRequest(5, 5, "ok")));

        assertEquals("Hanya pesanan dengan status COMPLETED yang dapat diberi rating.", exception.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
        verifyNoInteractions(outboxEventRepository);
    }

    @Test
    void testGiveRatingThrowsWhenOrderAlreadyRated() {
        order.setStatus(OrderStatus.COMPLETED);
        order.setJastiperRating(5);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));

        InvalidOrderException exception = assertThrows(InvalidOrderException.class,
                () -> orderService.giveRating(orderId, new OrderRatingRequest(4, 4, "retry")));

        assertEquals("Pesanan ini sudah diberi rating sebelumnya.", exception.getMessage());
        verify(orderRepository, never()).save(any(Order.class));
        verifyNoInteractions(outboxEventRepository);
    }

    @Test
    void testGiveRatingThrowsWhenOrderMissing() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class,
                () -> orderService.giveRating(orderId, new OrderRatingRequest(4, 5, "review")));
        verifyNoInteractions(outboxEventRepository);
    }
}
