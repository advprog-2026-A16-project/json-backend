package id.ac.ui.cs.advprog.jsonbackend.inventory.controller;

import id.ac.ui.cs.advprog.jsonbackend.inventory.dto.ProductRequest;
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
        when(productService.create(request)).thenReturn(response);

        ProductResponse result = productController.createProduct(request);

        assertEquals(response.getId(), result.getId());
        verify(productService, times(1)).create(request);
    }

    @Test
    void updateProductShouldDelegateToService() {
        UUID id = UUID.randomUUID();
        ProductRequest request = sampleRequest();
        ProductResponse response = sampleResponse();
        response.setId(id);

        when(productService.update(id, request)).thenReturn(response);

        ProductResponse result = productController.updateProduct(id, request);

        assertEquals(id, result.getId());
        verify(productService, times(1)).update(id, request);
    }

    @Test
    void deleteProductShouldDelegateToService() {
        UUID id = UUID.randomUUID();

        productController.deleteProduct(id);

        verify(productService, times(1)).delete(id);
    }

    @Test
    void reserveStockShouldDelegateToService() {
        UUID id = UUID.randomUUID();

        productController.reserveStock(id, 3);

        verify(productService, times(1)).reserveStock(id, 3);
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
}
