package id.ac.ui.cs.advprog.jsonbackend.inventory.controller;

import id.ac.ui.cs.advprog.jsonbackend.auth.enums.Role;
import id.ac.ui.cs.advprog.jsonbackend.auth.model.User;
import id.ac.ui.cs.advprog.jsonbackend.inventory.dto.ProductRequest;
import id.ac.ui.cs.advprog.jsonbackend.inventory.dto.ProductResponse;
import id.ac.ui.cs.advprog.jsonbackend.inventory.service.ProductService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminProductControllerTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private AdminProductController adminProductController;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void getAllProductsForAdminShouldDelegateToService() {
        authenticateAs(Role.ADMIN);
        ProductResponse response = sampleResponse();
        when(productService.findAll()).thenReturn(List.of(response));

        List<ProductResponse> result = adminProductController.getAllProductsForAdmin();

        assertEquals(1, result.size());
        assertEquals(response.getId(), result.getFirst().getId());
        verify(productService, times(1)).findAll();
    }

    @Test
    void getAllProductsForAdminShouldRejectNonAdmin() {
        authenticateAs(Role.TITIPERS);

        assertThrows(ResponseStatusException.class, () -> adminProductController.getAllProductsForAdmin());
        verify(productService, never()).findAll();
    }

    @Test
    void updateProductForAdminShouldDelegateToService() {
        authenticateAs(Role.ADMIN);
        UUID id = UUID.randomUUID();
        ProductRequest request = sampleRequest();
        ProductResponse response = sampleResponse();
        response.setId(id);
        when(productService.update(id, request)).thenReturn(response);

        ProductResponse result = adminProductController.updateProductForAdmin(id, request);

        assertEquals(id, result.getId());
        verify(productService, times(1)).update(id, request);
    }

    @Test
    void updateProductForAdminShouldRejectNonAdmin() {
        authenticateAs(Role.TITIPERS);
        UUID id = UUID.randomUUID();
        ProductRequest request = sampleRequest();

        assertThrows(ResponseStatusException.class, () -> adminProductController.updateProductForAdmin(id, request));
        verify(productService, never()).update(id, request);
    }

    @Test
    void deleteProductForAdminShouldDelegateToService() {
        authenticateAs(Role.ADMIN);
        UUID id = UUID.randomUUID();

        adminProductController.deleteProductForAdmin(id);

        verify(productService, times(1)).delete(id);
    }

    @Test
    void deleteProductForAdminShouldRejectNonAdmin() {
        authenticateAs(Role.JASTIPER);
        UUID id = UUID.randomUUID();

        assertThrows(ResponseStatusException.class, () -> adminProductController.deleteProductForAdmin(id));
        verify(productService, never()).delete(id);
    }

    private ProductRequest sampleRequest() {
        return ProductRequest.builder()
                .name("Updated Product")
                .description("Updated Description")
                .price(new BigDecimal("2000000"))
                .stock(8)
                .originCountry("JP")
                .purchaseDate(LocalDate.now().plusDays(5))
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

    private void authenticateAs(Role role) {
        User user = new User("admin@test.com", "secret", role);
        UsernamePasswordAuthenticationToken authentication =
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }
}
