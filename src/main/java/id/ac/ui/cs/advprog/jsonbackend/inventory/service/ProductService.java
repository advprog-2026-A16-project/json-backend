package id.ac.ui.cs.advprog.jsonbackend.inventory.service;

import id.ac.ui.cs.advprog.jsonbackend.auth.enums.Role;
import id.ac.ui.cs.advprog.jsonbackend.inventory.dto.ProductRequest;
import id.ac.ui.cs.advprog.jsonbackend.inventory.dto.ProductResponse;

import java.util.List;
import java.util.UUID;

public interface ProductService {
    List<ProductResponse> findAll();
    List<ProductResponse> findAll(int page, int size, String sortBy, String direction);

    List<ProductResponse> searchByKeyword(String keyword);
    List<ProductResponse> searchByKeyword(String keyword, int page, int size, String sortBy, String direction);

    List<ProductResponse> findByJastiperId(UUID jastiperId);
    List<ProductResponse> findByJastiperId(UUID jastiperId, int page, int size, String sortBy, String direction);

    void reserveStock(UUID productId, int quantity);

    void releaseStock(UUID productId, int quantity);

    ProductResponse findById(UUID id);

    ProductResponse create(ProductRequest request);

    ProductResponse createAsJastiper(ProductRequest request, UUID actorId, Role actorRole);

    ProductResponse update(UUID id, ProductRequest request);

    ProductResponse updateAsJastiper(UUID id, ProductRequest request, UUID actorId, Role actorRole);

    void delete(UUID id);

    void deleteAsJastiper(UUID id, UUID actorId, Role actorRole);
}
