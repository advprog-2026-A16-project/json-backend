package id.ac.ui.cs.advprog.jsonbackend.inventory.controller;

import id.ac.ui.cs.advprog.jsonbackend.auth.enums.Role;
import id.ac.ui.cs.advprog.jsonbackend.auth.model.User;
import id.ac.ui.cs.advprog.jsonbackend.inventory.dto.ProductRequest;
import id.ac.ui.cs.advprog.jsonbackend.inventory.dto.ProductResponse;
import id.ac.ui.cs.advprog.jsonbackend.inventory.exception.InvalidProductException;
import id.ac.ui.cs.advprog.jsonbackend.inventory.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductControllerTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    @Test
    void getAllProductsShouldReturnListFromService() {
        ProductResponse response = sampleResponse();
        when(productService.findAll()).thenReturn(List.of(response));

        List<ProductResponse> result = productController.getAllProducts();

        assertEquals(1, result.size());
        assertEquals("Sneakers Limited Edition", result.getFirst().getName());
        verify(productService, times(1)).findAll();
    }

    @Test
    void searchProductsShouldDelegateToService() {
        String keyword = "sneakers";
        ProductResponse response = sampleResponse();
        when(productService.searchByKeyword(keyword)).thenReturn(List.of(response));

        List<ProductResponse> result = productController.searchProducts(keyword);

        assertEquals(1, result.size());
        assertEquals(response.getId(), result.getFirst().getId());
        verify(productService, times(1)).searchByKeyword(keyword);
    }

    @Test
    void getProductsByJastiperShouldDelegateToService() {
        UUID jastiperId = UUID.randomUUID();
        ProductResponse response = sampleResponse();
        when(productService.findByJastiperId(jastiperId)).thenReturn(List.of(response));

        List<ProductResponse> result = productController.getProductsByJastiper(jastiperId);

        assertEquals(1, result.size());
        assertEquals(response.getId(), result.getFirst().getId());
        verify(productService, times(1)).findByJastiperId(jastiperId);
    }

    @Test
    void getProductByIdShouldReturnServiceResult() {
        UUID id = UUID.randomUUID();
        ProductResponse response = sampleResponse();
        response.setId(id);
        when(productService.findById(id)).thenReturn(response);

        ProductResponse result = productController.getProductById(id);

        assertEquals(id, result.getId());
        verify(productService, times(1)).findById(id);
    }

    @Test
    void createProductShouldDelegateToService() {
        ProductRequest request = sampleRequest();
        ProductResponse response = sampleResponse();
        UUID actorId = UUID.randomUUID();
        mockAuthenticatedJastiper(actorId);
        when(productService.createAsJastiper(request, actorId, Role.JASTIPER)).thenReturn(response);

        ProductResponse result = productController.createProduct(request);

        assertEquals(response.getId(), result.getId());
        verify(productService, times(1)).createAsJastiper(request, actorId, Role.JASTIPER);
    }

    @Test
    void updateProductShouldDelegateToService() {
        UUID id = UUID.randomUUID();
        ProductRequest request = sampleRequest();
        ProductResponse response = sampleResponse();
        response.setId(id);
        UUID actorId = UUID.randomUUID();
        mockAuthenticatedJastiper(actorId);

        when(productService.updateAsJastiper(id, request, actorId, Role.JASTIPER)).thenReturn(response);

        ProductResponse result = productController.updateProduct(id, request);

        assertEquals(id, result.getId());
        verify(productService, times(1)).updateAsJastiper(id, request, actorId, Role.JASTIPER);
    }

    @Test
    void deleteProductShouldDelegateToService() {
        UUID id = UUID.randomUUID();
        UUID actorId = UUID.randomUUID();
        mockAuthenticatedJastiper(actorId);

        productController.deleteProduct(id);

        verify(productService, times(1)).deleteAsJastiper(id, actorId, Role.JASTIPER);
    }

    @Test
    void reserveStockShouldDelegateToService() {
        UUID id = UUID.randomUUID();

        productController.reserveStock(id, 3);

        verify(productService, times(1)).reserveStock(id, 3);
    }

    @Test
    void reserveStockShouldRejectNonPositiveQuantity() {
        UUID id = UUID.randomUUID();

        assertThrows(InvalidProductException.class, () -> productController.reserveStock(id, 0));
        verify(productService, times(0)).reserveStock(id, 0);
    }

    private ProductRequest sampleRequest() {
        return ProductRequest.builder()
                .name("Sneakers Limited Edition")
                .description("Beli di US")
                .price(new BigDecimal("1500000"))
                .stock(10)
                .originCountry("US")
                .purchaseDate(LocalDate.now().plusDays(7))
                .jastiperId(UUID.randomUUID())
                .build();
    }

    private ProductResponse sampleResponse() {
        LocalDateTime now = LocalDateTime.now();
        return ProductResponse.builder()
                .id(UUID.randomUUID())
                .name("Sneakers Limited Edition")
                .description("Beli di US")
                .price(new BigDecimal("1500000"))
                .stock(10)
                .originCountry("US")
                .purchaseDate(LocalDate.now().plusDays(7))
                .jastiperId(UUID.randomUUID())
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    private void mockAuthenticatedJastiper(UUID actorId) {
        User actor = User.builder()
                .id(actorId)
                .email("jastiper@test.com")
                .password("x")
                .role(Role.JASTIPER)
                .build();
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(actor, null, actor.getAuthorities())
        );
    }
}
