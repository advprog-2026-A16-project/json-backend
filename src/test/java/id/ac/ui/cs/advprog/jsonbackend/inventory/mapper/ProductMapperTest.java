package id.ac.ui.cs.advprog.jsonbackend.inventory.mapper;

import id.ac.ui.cs.advprog.jsonbackend.inventory.dto.ProductRequest;
import id.ac.ui.cs.advprog.jsonbackend.inventory.dto.ProductResponse;
import id.ac.ui.cs.advprog.jsonbackend.inventory.model.Product;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class ProductMapperTest {

    private final ProductMapper productMapper = new ProductMapper();

    @Test
    void toEntityMapsAndNormalizesImageUrl() {
        UUID jastiperId = UUID.randomUUID();
        ProductRequest request = ProductRequest.builder()
                .name("Camera")
                .description("Mirrorless")
                .imageUrl(" https://cdn.example.com/camera.jpg ")
                .price(new BigDecimal("2500000"))
                .stock(3)
                .originCountry("JP")
                .purchaseDate(LocalDate.of(2026, 5, 21))
                .jastiperId(jastiperId)
                .build();

        Product result = productMapper.toEntity(request);

        assertEquals("Camera", result.getName());
        assertEquals("Mirrorless", result.getDescription());
        assertEquals("https://cdn.example.com/camera.jpg", result.getImageUrl());
        assertEquals(new BigDecimal("2500000"), result.getPrice());
        assertEquals(3, result.getStock());
        assertEquals("JP", result.getOriginCountry());
        assertEquals(LocalDate.of(2026, 5, 21), result.getPurchaseDate());
        assertEquals(jastiperId, result.getJastiperId());
    }

    @Test
    void toEntityMapsBlankImageUrlToNull() {
        ProductRequest request = ProductRequest.builder()
                .name("Camera")
                .description("Mirrorless")
                .imageUrl("   ")
                .price(new BigDecimal("2500000"))
                .stock(3)
                .originCountry("JP")
                .purchaseDate(LocalDate.of(2026, 5, 21))
                .jastiperId(UUID.randomUUID())
                .build();

        Product result = productMapper.toEntity(request);

        assertNull(result.getImageUrl());
    }

    @Test
    void updateEntityOverwritesAllFields() {
        UUID jastiperId = UUID.randomUUID();
        Product existing = Product.builder()
                .name("Old")
                .description("Old desc")
                .imageUrl("https://old.example.com/image.jpg")
                .price(new BigDecimal("1000"))
                .stock(1)
                .originCountry("ID")
                .purchaseDate(LocalDate.of(2026, 1, 1))
                .jastiperId(UUID.randomUUID())
                .build();
        ProductRequest request = ProductRequest.builder()
                .name("New")
                .description("New desc")
                .imageUrl("   ")
                .price(new BigDecimal("2000"))
                .stock(5)
                .originCountry("US")
                .purchaseDate(LocalDate.of(2026, 2, 2))
                .jastiperId(jastiperId)
                .build();

        productMapper.updateEntity(existing, request);

        assertEquals("New", existing.getName());
        assertEquals("New desc", existing.getDescription());
        assertNull(existing.getImageUrl());
        assertEquals(new BigDecimal("2000"), existing.getPrice());
        assertEquals(5, existing.getStock());
        assertEquals("US", existing.getOriginCountry());
        assertEquals(LocalDate.of(2026, 2, 2), existing.getPurchaseDate());
        assertEquals(jastiperId, existing.getJastiperId());
    }

    @Test
    void toResponseMapsAllFields() {
        UUID id = UUID.randomUUID();
        UUID jastiperId = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        Product product = Product.builder()
                .id(id)
                .name("Switch")
                .description("Console")
                .imageUrl("https://cdn.example.com/switch.jpg")
                .price(new BigDecimal("5000000"))
                .stock(8)
                .originCountry("JP")
                .purchaseDate(LocalDate.of(2026, 3, 3))
                .jastiperId(jastiperId)
                .createdAt(now)
                .updatedAt(now)
                .build();

        ProductResponse result = productMapper.toResponse(product);

        assertEquals(id, result.getId());
        assertEquals("Switch", result.getName());
        assertEquals("Console", result.getDescription());
        assertEquals("https://cdn.example.com/switch.jpg", result.getImageUrl());
        assertEquals(new BigDecimal("5000000"), result.getPrice());
        assertEquals(8, result.getStock());
        assertEquals("JP", result.getOriginCountry());
        assertEquals(LocalDate.of(2026, 3, 3), result.getPurchaseDate());
        assertEquals(jastiperId, result.getJastiperId());
        assertEquals(now, result.getCreatedAt());
        assertEquals(now, result.getUpdatedAt());
    }
}
