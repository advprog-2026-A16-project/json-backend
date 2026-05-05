package id.ac.ui.cs.advprog.jsonbackend.inventory.controller;

import id.ac.ui.cs.advprog.jsonbackend.auth.model.User;
import id.ac.ui.cs.advprog.jsonbackend.inventory.dto.ProductRequest;
import id.ac.ui.cs.advprog.jsonbackend.inventory.dto.ProductResponse;
import id.ac.ui.cs.advprog.jsonbackend.inventory.exception.ForbiddenInventoryAccessException;
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
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
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
import java.util.Set;
import java.util.UUID;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Inventory", description = "Inventory and catalog endpoints")
public class ProductController {
    private static final Set<String> ALLOWED_SORT_FIELDS = Set.of(
            "createdAt", "updatedAt", "name", "price", "stock", "purchaseDate"
    );

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping
    @Operation(summary = "Get all products")
    @ApiResponse(responseCode = "200", description = "Success",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ProductResponse.class))))
    public List<ProductResponse> getAllProducts(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        validateListParams(page, size, sortBy);
        return productService.findAll(page, size, sortBy, direction);
    }

    @GetMapping("/search")
    @Operation(summary = "Search products by keyword")
    @ApiResponse(responseCode = "200", description = "Success",
            content = @Content(array = @ArraySchema(schema = @Schema(implementation = ProductResponse.class))))
    public List<ProductResponse> searchProducts(
            @RequestParam String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        validateListParams(page, size, sortBy);
        return productService.searchByKeyword(keyword, page, size, sortBy, direction);
    }

    @GetMapping("/jastiper/{jastiperId}")
    @Operation(summary = "Get products by jastiper id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Success",
                    content = @Content(array = @ArraySchema(schema = @Schema(implementation = ProductResponse.class)))),
            @ApiResponse(responseCode = "400", description = "Invalid jastiper id")
    })
    public List<ProductResponse> getProductsByJastiper(
            @PathVariable UUID jastiperId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "desc") String direction
    ) {
        validateListParams(page, size, sortBy);
        return productService.findByJastiperId(jastiperId, page, size, sortBy, direction);
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
        User actor = getAuthenticatedUser();
        return productService.createAsJastiper(request, actor.getId(), actor.getRole());
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
        User actor = getAuthenticatedUser();
        return productService.updateAsJastiper(id, request, actor.getId(), actor.getRole());
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
        User actor = getAuthenticatedUser();
        productService.deleteAsJastiper(id, actor.getId(), actor.getRole());
    }

    @PostMapping("/{id}/reserve")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(
            summary = "Reserve stock for checkout flow",
            description = """
                    Reserves stock synchronously and appends outbox event (STOCK_RESERVED) for async event handling.
                    """,
            security = @SecurityRequirement(name = "bearerAuth")
    )
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

    private User getAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof User user)) {
            throw new ForbiddenInventoryAccessException("Authenticated user is required");
        }
        return user;
    }

    private void validateListParams(int page, int size, String sortBy) {
        if (page < 0) {
            throw new InvalidProductException("Page must be zero or greater");
        }
        if (size <= 0) {
            throw new InvalidProductException("Size must be greater than zero");
        }
        String safeSortBy = (sortBy == null || sortBy.isBlank()) ? "createdAt" : sortBy;
        if (!ALLOWED_SORT_FIELDS.contains(safeSortBy)) {
            throw new InvalidProductException("Unsupported sort field: " + safeSortBy);
        }
    }
}
