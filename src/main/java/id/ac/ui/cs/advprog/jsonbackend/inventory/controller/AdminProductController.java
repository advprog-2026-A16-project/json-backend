package id.ac.ui.cs.advprog.jsonbackend.inventory.controller;

import id.ac.ui.cs.advprog.jsonbackend.inventory.dto.ProductResponse;
import id.ac.ui.cs.advprog.jsonbackend.inventory.service.ProductService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/products")
public class AdminProductController {

    private final ProductService productService;

    public AdminProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    public List<ProductResponse> getAllProductsForAdmin() {
        return productService.findAll();
    }
}
