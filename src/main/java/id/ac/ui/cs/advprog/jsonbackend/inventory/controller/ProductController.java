package id.ac.ui.cs.advprog.jsonbackend.inventory.controller;

import id.ac.ui.cs.advprog.jsonbackend.inventory.dto.ProductRequest;
import id.ac.ui.cs.advprog.jsonbackend.inventory.dto.ProductResponse;
import id.ac.ui.cs.advprog.jsonbackend.inventory.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<ProductResponse> getAllProducts() {
        return productService.findAll();
    }

    @GetMapping("/search")
    public List<ProductResponse> searchProducts(@RequestParam String keyword) {
        return productService.searchByKeyword(keyword);
    }

    @GetMapping("/jastiper/{jastiperId}")
    public List<ProductResponse> getProductsByJastiper(@PathVariable UUID jastiperId) {
        return productService.findByJastiperId(jastiperId);
    }

    @GetMapping("/{id}")
    public ProductResponse getProductById(@PathVariable UUID id) {
        return productService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ProductResponse createProduct(@Valid @RequestBody ProductRequest request) {
        return productService.create(request);
    }

    @PutMapping("/{id}")
    public ProductResponse updateProduct(@PathVariable UUID id, @Valid @RequestBody ProductRequest request) {
        return productService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteProduct(@PathVariable UUID id) {
        productService.delete(id);
    }

    @PostMapping("/{id}/reserve")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reserveStock(@PathVariable UUID id, @RequestParam int quantity) {
        // RED skeleton: service delegation will be added in GREEN.
    }
}
