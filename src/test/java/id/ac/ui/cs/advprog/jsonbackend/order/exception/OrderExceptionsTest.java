package id.ac.ui.cs.advprog.jsonbackend.order.exception;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class OrderExceptionsTest {

    @Test
    void invalidOrderExceptionStoresMessage() {
        InvalidOrderException exception = new InvalidOrderException("invalid");
        assertEquals("invalid", exception.getMessage());
    }

    @Test
    void orderNotFoundExceptionStoresMessage() {
        OrderNotFoundException exception = new OrderNotFoundException("missing");
        assertEquals("missing", exception.getMessage());
    }

    @Test
    void invalidOrderStatusExceptionStoresMessage() {
        InvalidOrderStatusException exception = new InvalidOrderStatusException("bad status");
        assertEquals("bad status", exception.getMessage());
    }
}
