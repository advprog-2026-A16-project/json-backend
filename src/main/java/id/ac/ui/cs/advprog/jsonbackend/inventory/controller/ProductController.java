package id.ac.ui.cs.advprog.jsonbackend.inventory.controller;

import id.ac.ui.cs.advprog.jsonbackend.inventory.model.Product;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    @GetMapping
    public List<Product> getAllProducts() {
        return List.of(
            Product.builder()
                .id("1")
                .name("Limited Edition Sneakers")
                .description("Exclusive sneakers from Tokyo")
                .price(new BigDecimal("2500000"))
                .stock(5)
                .originCountry("Japan")
                .jastiperId("jastiper-001")
                .build(),
            Product.builder()
                .id("2")
                .name("Korean Skincare Set")
                .description("Popular K-beauty products")
                .price(new BigDecimal("750000"))
                .stock(10)
                .originCountry("South Korea")
                .jastiperId("jastiper-002")
                .build()
        );
    }

    @GetMapping("/{id}")
    public Product getProductById(@PathVariable String id) {
        return Product.builder()
            .id(id)
            .name("Sample Product")
            .description("This is a sample product")
            .price(new BigDecimal("100000"))
            .stock(10)
            .originCountry("Japan")
            .jastiperId("jastiper-001")
            .build();
    }
}
