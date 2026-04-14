package id.ac.ui.cs.advprog.jsonbackend.inventory.controller;

import id.ac.ui.cs.advprog.jsonbackend.inventory.dto.ProductResponse;
import id.ac.ui.cs.advprog.jsonbackend.inventory.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AdminProductControllerTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private AdminProductController adminProductController;

    @Test
    void getAllProductsForAdminShouldDelegateToService() {
        ProductResponse response = sampleResponse();
        when(productService.findAll()).thenReturn(List.of(response));

        List<ProductResponse> result = adminProductController.getAllProductsForAdmin();

        assertEquals(1, result.size());
        assertEquals(response.getId(), result.getFirst().getId());
        verify(productService, times(1)).findAll();
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
}
