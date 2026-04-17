package id.ac.ui.cs.advprog.jsonbackend.order.service;

import id.ac.ui.cs.advprog.jsonbackend.inventory.dto.ProductResponse;
import id.ac.ui.cs.advprog.jsonbackend.inventory.service.ProductService;
import id.ac.ui.cs.advprog.jsonbackend.order.dto.OrderRequest;
import id.ac.ui.cs.advprog.jsonbackend.order.dto.OrderResponse;
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

    @InjectMocks
    private OrderServiceImpl orderService;

    private UUID orderId;
    private UUID productId;
    private UUID titipersId;
    private UUID jastiperId;

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
        order.setStatus(OrderStatus.PAID);

        orderResponse = new OrderResponse();
        orderResponse.setId(orderId);
        orderResponse.setProductId(productId);
        orderResponse.setTitipersId(titipersId);
        orderResponse.setJastiperId(jastiperId);
        orderResponse.setQuantity(2);
        orderResponse.setStatus(OrderStatus.PAID);
    }

    @Test
    void testCreateOrderSuccess() {
        OrderRequest request = new OrderRequest();
        request.setProductId(productId);
        request.setQuantity(2);
        request.setTitipersId(titipersId);

        ProductResponse productResponse = new ProductResponse();
        productResponse.setId(productId);
        productResponse.setJastiperId(jastiperId);
        productResponse.setPrice(new BigDecimal("50000"));
        productResponse.setStock(10); // Stok aman

        // Mocking behavior
        when(productService.findById(productId)).thenReturn(productResponse);
        when(orderMapper.toEntity(request)).thenReturn(order);
        when(orderRepository.save(any(Order.class))).thenReturn(order);
        when(orderMapper.toResponse(order)).thenReturn(orderResponse);

        // Eksekusi
        OrderResponse result = orderService.create(request);

        // Verifikasi
        assertNotNull(result);
        assertEquals(orderId, result.getId());
        assertEquals(jastiperId, result.getJastiperId());

        // Pastikan service manggil reserveStock untuk ngurangin inventory
        verify(productService).reserveStock(productId, 2);
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void testCreateOrderThrowsExceptionWhenStockInsufficient() {
        OrderRequest request = new OrderRequest();
        request.setProductId(productId);
        request.setQuantity(15); // Minta 15

        ProductResponse productResponse = new ProductResponse();
        productResponse.setId(productId);
        productResponse.setStock(10); // Stok cuma 10

        when(productService.findById(productId)).thenReturn(productResponse);

        // Eksekusi & Verifikasi Exception
        InvalidOrderException exception = assertThrows(InvalidOrderException.class, () -> {
            orderService.create(request);
        });

        assertEquals("Stok tidak mencukupi untuk pesanan ini", exception.getMessage());

        // Pastikan nggak nyimpen ke database atau ngurangin stok kalau gagal
        verify(productService, never()).reserveStock(any(UUID.class), anyInt());
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void testFindAllReturnsListOfOrders() {
        when(orderRepository.findAll()).thenReturn(List.of(order));
        when(orderMapper.toResponse(order)).thenReturn(orderResponse);

        List<OrderResponse> result = orderService.findAll();

        assertFalse(result.isEmpty());
        assertEquals(1, result.size());
        assertEquals(orderId, result.get(0).getId());
        verify(orderRepository).findAll();
    }

    @Test
    void testFindByIdSuccess() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(orderMapper.toResponse(order)).thenReturn(orderResponse);

        OrderResponse result = orderService.findById(orderId);

        assertNotNull(result);
        assertEquals(orderId, result.getId());
        verify(orderRepository).findById(orderId);
    }

    @Test
    void testFindByIdThrowsExceptionWhenNotFound() {
        when(orderRepository.findById(orderId)).thenReturn(Optional.empty());

        assertThrows(OrderNotFoundException.class, () -> {
            orderService.findById(orderId);
        });

        verify(orderRepository).findById(orderId);
    }

    @Test
    void testFindByTitipersIdReturnsListOfOrders() {
        when(orderRepository.findByTitipersId(titipersId)).thenReturn(List.of(order));
        when(orderMapper.toResponse(order)).thenReturn(orderResponse);

        List<OrderResponse> result = orderService.findByTitipersId(titipersId);

        assertFalse(result.isEmpty());
        assertEquals(titipersId, result.get(0).getTitipersId());
        verify(orderRepository).findByTitipersId(titipersId);
    }

    @Test
    void testFindByJastiperIdReturnsListOfOrders() {
        when(orderRepository.findByJastiperId(jastiperId)).thenReturn(List.of(order));
        when(orderMapper.toResponse(order)).thenReturn(orderResponse);

        List<OrderResponse> result = orderService.findByJastiperId(jastiperId);

        assertFalse(result.isEmpty());
        assertEquals(jastiperId, result.get(0).getJastiperId());
        verify(orderRepository).findByJastiperId(jastiperId);
    }
}