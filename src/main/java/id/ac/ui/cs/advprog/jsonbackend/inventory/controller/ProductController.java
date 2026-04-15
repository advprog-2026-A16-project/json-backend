package id.ac.ui.cs.advprog.jsonbackend.inventory.controller;

import id.ac.ui.cs.advprog.jsonbackend.inventory.dto.ProductRequest;
import id.ac.ui.cs.advprog.jsonbackend.inventory.dto.ProductResponse;
import id.ac.ui.cs.advprog.jsonbackend.inventory.exception.InvalidProductException;
import id.ac.ui.cs.advprog.jsonbackend.inventory.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Inventory", description = "Inventory and catalog endpoints")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    @Operation(summary = "Get all products")
    @ApiResponse(responseCode = "200", description = "Success",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ProductResponse.class))))
    public List<ProductResponse> getAllProducts() {
        return productService.findAll();
    }

    @GetMapping("/search")
    @Operation(summary = "Search products by keyword")
    @ApiResponse(responseCode = "200", description = "Success",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ProductResponse.class))))
    public List<ProductResponse> searchProducts(@RequestParam String keyword) {
        return productService.searchByKeyword(keyword);
    }

    @GetMapping("/jastiper/{jastiperId}")
    @Operation(summary = "Get products by jastiper id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ProductResponse.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid jastiper id")
    })
    public List<ProductResponse> getProductsByJastiper(@PathVariable UUID jastiperId) {
        return productService.findByJastiperId(jastiperId);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Get product detail by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success",
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "404", description = "Product not found")
    })
    public ProductResponse getProductById(@PathVariable UUID id) {
        return productService.findById(id);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Create product", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Created",
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ProductResponse createProduct(@Valid @RequestBody ProductRequest request) {
        return productService.create(request);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update product", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Updated",
                    content = @Content(schema = @Schema(implementation = ProductResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid request"),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public ProductResponse updateProduct(@PathVariable UUID id, @Valid @RequestBody ProductRequest request) {
        return productService.update(id, request);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Delete product", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Deleted"),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public void deleteProduct(@PathVariable UUID id) {
        productService.delete(id);
    }

    @PostMapping("/{id}/reserve")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Reserve stock for checkout flow", security = @SecurityRequirement(name = "bearerAuth"))
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Reserved"),
            @ApiResponse(responseCode = "400", description = "Invalid quantity"),
            @ApiResponse(responseCode = "404", description = "Product not found"),
            @ApiResponse(responseCode = "409", description = "Insufficient stock"),
            @ApiResponse(responseCode = "403", description = "Forbidden")
    })
    public void reserveStock(@PathVariable UUID id, @RequestParam int quantity) {
        if (quantity <= 0) {
            throw new InvalidProductException("Quantity must be greater than zero");
        }
        productService.reserveStock(id, quantity);
    }
}
