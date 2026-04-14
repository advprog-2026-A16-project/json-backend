package id.ac.ui.cs.advprog.jsonbackend.inventory.controller;

import id.ac.ui.cs.advprog.jsonbackend.auth.exception.GlobalExceptionHandler;
import id.ac.ui.cs.advprog.jsonbackend.inventory.exception.InsufficientStockException;
import id.ac.ui.cs.advprog.jsonbackend.inventory.exception.ProductNotFoundException;
import id.ac.ui.cs.advprog.jsonbackend.inventory.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.UUID;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
class ProductControllerWebMvcTest {

    @Mock
    private ProductService productService;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        ProductController controller = new ProductController(productService);
        mockMvc = MockMvcBuilders
                .standaloneSetup(controller)
                .setControllerAdvice(new GlobalExceptionHandler())
                .build();
    }

    @Test
    void reserveStockReturnsBadRequestWhenQuantityNonPositive() throws Exception {
        UUID id = UUID.randomUUID();

        mockMvc.perform(post("/api/products/{id}/reserve", id)
                        .queryParam("quantity", "0"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Quantity must be greater than zero"));

        verifyNoInteractions(productService);
    }

    @Test
    void reserveStockReturnsConflictWhenInsufficientStock() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new InsufficientStockException("Insufficient stock"))
                .when(productService)
                .reserveStock(id, 5);

        mockMvc.perform(post("/api/products/{id}/reserve", id)
                        .queryParam("quantity", "5"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("Insufficient stock"));
    }

    @Test
    void reserveStockReturnsNotFoundWhenProductMissing() throws Exception {
        UUID id = UUID.randomUUID();
        doThrow(new ProductNotFoundException("Product not found with id: " + id))
                .when(productService)
                .reserveStock(id, 1);

        mockMvc.perform(post("/api/products/{id}/reserve", id)
                        .queryParam("quantity", "1"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("Product not found with id: " + id));
    }
}
