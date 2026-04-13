package id.ac.ui.cs.advprog.jsonbackend.inventory.model;

import id.ac.ui.cs.advprog.jsonbackend.inventory.exception.InsufficientStockException;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductTest {

    @Test
    void reduceStockSuccess() {
        Product product = sampleProduct(10);

        product.reduceStock(3);

        assertEquals(7, product.getStock());
    }

    @Test
    void reduceStockThrowsWhenInsufficient() {
        Product product = sampleProduct(2);

        assertThrows(InsufficientStockException.class, () -> product.reduceStock(3));
    }

    private Product sampleProduct(int stock) {
        LocalDateTime now = LocalDateTime.now();
        return Product.builder()
                .id(UUID.randomUUID())
                .name("Sample Product")
                .description("Sample Description")
                .price(new BigDecimal("10000"))
                .stock(stock)
                .originCountry("ID")
                .purchaseDate(LocalDate.now())
                .jastiperId(UUID.randomUUID())
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}
