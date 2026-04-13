package id.ac.ui.cs.advprog.jsonbackend.inventory.service;

import id.ac.ui.cs.advprog.jsonbackend.inventory.dto.ProductRequest;
import id.ac.ui.cs.advprog.jsonbackend.inventory.dto.ProductResponse;
import id.ac.ui.cs.advprog.jsonbackend.inventory.exception.InvalidProductException;
import id.ac.ui.cs.advprog.jsonbackend.inventory.exception.ProductNotFoundException;
import id.ac.ui.cs.advprog.jsonbackend.inventory.mapper.ProductMapper;
import id.ac.ui.cs.advprog.jsonbackend.inventory.model.Product;
import id.ac.ui.cs.advprog.jsonbackend.inventory.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductMapper productMapper;

    public ProductServiceImpl(ProductRepository productRepository, ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.productMapper = productMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> findAll() {
        return productRepository.findAll().stream()
                .map(productMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> searchByKeyword(String keyword) {
        String normalizedKeyword = keyword == null ? "" : keyword.trim();
        if (normalizedKeyword.isEmpty()) {
            return productRepository.findAll().stream()
                    .map(productMapper::toResponse)
                    .toList();
        }
        return productRepository
                .findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(normalizedKeyword, normalizedKeyword)
                .stream()
                .map(productMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> findByJastiperId(UUID jastiperId) {
        if (jastiperId == null) {
            throw new InvalidProductException("Jastiper id is required");
        }
        return productRepository.findByJastiperId(jastiperId).stream()
                .map(productMapper::toResponse)
                .toList();
    }

    @Override
    public void reserveStock(UUID productId, int quantity) {
        Product existing = getProductOrThrow(productId);
        existing.reduceStock(quantity);
        productRepository.save(existing);
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse findById(UUID id) {
        Product product = getProductOrThrow(id);
        return productMapper.toResponse(product);
    }

    @Override
    public ProductResponse create(ProductRequest request) {
        validateBusinessRules(request);

        Product product = productMapper.toEntity(request);
        Product saved = productRepository.save(product);
        return productMapper.toResponse(saved);
    }

    @Override
    public ProductResponse update(UUID id, ProductRequest request) {
        validateBusinessRules(request);

        Product existing = getProductOrThrow(id);
        productMapper.updateEntity(existing, request);

        Product saved = productRepository.save(existing);
        return productMapper.toResponse(saved);
    }

    @Override
    public void delete(UUID id) {
        Product existing = getProductOrThrow(id);
        productRepository.delete(existing);
    }

    private Product getProductOrThrow(UUID id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + id));
    }

    private void validateBusinessRules(ProductRequest request) {
        if (request == null) {
            throw new InvalidProductException("Product request is required");
        }
        if (request.getJastiperId() == null) {
            throw new InvalidProductException("Jastiper id is required");
        }
        if (request.getPrice() == null || request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new InvalidProductException("Price must be greater than zero");
        }
        if (request.getStock() == null || request.getStock() < 0) {
            throw new InvalidProductException("Stock cannot be negative");
        }
    }
}
