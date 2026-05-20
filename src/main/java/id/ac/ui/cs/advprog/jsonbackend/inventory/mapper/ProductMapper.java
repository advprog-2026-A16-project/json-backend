package id.ac.ui.cs.advprog.jsonbackend.inventory.mapper;

import id.ac.ui.cs.advprog.jsonbackend.inventory.dto.ProductRequest;
import id.ac.ui.cs.advprog.jsonbackend.inventory.dto.ProductResponse;
import id.ac.ui.cs.advprog.jsonbackend.inventory.model.Product;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public Product toEntity(ProductRequest request) {
        return Product.builder()
                .name(request.getName())
                .description(request.getDescription())
                .imageUrl(normalizeImageUrl(request.getImageUrl()))
                .price(request.getPrice())
                .stock(request.getStock())
                .originCountry(request.getOriginCountry())
                .purchaseDate(request.getPurchaseDate())
                .jastiperId(request.getJastiperId())
                .build();
    }

    public void updateEntity(Product product, ProductRequest request) {
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setImageUrl(normalizeImageUrl(request.getImageUrl()));
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setOriginCountry(request.getOriginCountry());
        product.setPurchaseDate(request.getPurchaseDate());
        product.setJastiperId(request.getJastiperId());
    }

    public ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
                .id(product.getId())
                .name(product.getName())
                .description(product.getDescription())
                .imageUrl(product.getImageUrl())
                .price(product.getPrice())
                .stock(product.getStock())
                .originCountry(product.getOriginCountry())
                .purchaseDate(product.getPurchaseDate())
                .jastiperId(product.getJastiperId())
                .createdAt(product.getCreatedAt())
                .updatedAt(product.getUpdatedAt())
                .build();
    }

    private String normalizeImageUrl(String imageUrl) {
        if (imageUrl == null) {
            return null;
        }
        String trimmed = imageUrl.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
