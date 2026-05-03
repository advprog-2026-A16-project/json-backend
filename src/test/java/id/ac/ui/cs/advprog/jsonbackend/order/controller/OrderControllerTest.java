package id.ac.ui.cs.advprog.jsonbackend.order.controller;

import id.ac.ui.cs.advprog.jsonbackend.order.dto.OrderRequest;
import id.ac.ui.cs.advprog.jsonbackend.order.dto.OrderResponse;
import id.ac.ui.cs.advprog.jsonbackend.order.dto.OrderStatusUpdateRequest;
import id.ac.ui.cs.advprog.jsonbackend.order.model.OrderStatus;
import id.ac.ui.cs.advprog.jsonbackend.order.service.OrderService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OrderControllerTest {

    @Mock
    private OrderService orderService;

    @InjectMocks
    private OrderController orderController;

    private UUID orderId;
    private OrderResponse orderResponse;

    @BeforeEach
    void setUp() {
        orderId = UUID.randomUUID();
        orderResponse = new OrderResponse();
        orderResponse.setId(orderId);
        orderResponse.setStatus(OrderStatus.PAID);
    }

    @Test
    void getAllOrdersReturnsOkStatusAndResponse() {
        List<OrderResponse> expectedResponse = List.of(orderResponse);
        when(orderService.findAll()).thenReturn(expectedResponse);

        ResponseEntity<List<OrderResponse>> response = orderController.getAllOrders();

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
        verify(orderService).findAll();
    }

    @Test
    void getOrderByIdReturnsOkStatusAndResponse() {
        when(orderService.findById(orderId)).thenReturn(orderResponse);

        ResponseEntity<OrderResponse> response = orderController.getOrderById(orderId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(orderResponse, response.getBody());
        verify(orderService).findById(orderId);
    }

    @Test
    void createOrderReturnsCreatedStatusAndResponse() {
        OrderRequest request = new OrderRequest();
        when(orderService.create(request)).thenReturn(orderResponse);

        ResponseEntity<OrderResponse> response = orderController.createOrder(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode()); // Note: Status 201 Created
        assertEquals(orderResponse, response.getBody());
        verify(orderService).create(request);
    }

    @Test
    void updateStatusReturnsOkStatusAndResponse() {
        OrderStatusUpdateRequest request = new OrderStatusUpdateRequest();
        request.setNewStatus(OrderStatus.PURCHASED);
        when(orderService.updateStatus(orderId, request)).thenReturn(orderResponse);

        ResponseEntity<OrderResponse> response = orderController.updateStatus(orderId, request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(orderResponse, response.getBody());
        verify(orderService).updateStatus(orderId, request);
    }

    @Test
    void getOrdersByTitipersIdReturnsOkStatusAndResponse() {
        UUID titipersId = UUID.randomUUID();
        List<OrderResponse> expectedResponse = List.of(orderResponse);
        when(orderService.findByTitipersId(titipersId)).thenReturn(expectedResponse);

        ResponseEntity<List<OrderResponse>> response = orderController.getOrdersByTitipersId(titipersId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
        verify(orderService).findByTitipersId(titipersId);
    }

    @Test
    void getOrdersByJastiperIdReturnsOkStatusAndResponse() {
        UUID jastiperId = UUID.randomUUID();
        List<OrderResponse> expectedResponse = List.of(orderResponse);
        when(orderService.findByJastiperId(jastiperId)).thenReturn(expectedResponse);

        ResponseEntity<List<OrderResponse>> response = orderController.getOrdersByJastiperId(jastiperId);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals(expectedResponse, response.getBody());
        verify(orderService).findByJastiperId(jastiperId);
    }
}