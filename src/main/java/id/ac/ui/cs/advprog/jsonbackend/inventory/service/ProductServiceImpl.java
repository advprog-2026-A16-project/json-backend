package id.ac.ui.cs.advprog.jsonbackend.inventory.service;

import id.ac.ui.cs.advprog.jsonbackend.auth.enums.Role;
import id.ac.ui.cs.advprog.jsonbackend.inventory.dto.ProductRequest;
import id.ac.ui.cs.advprog.jsonbackend.inventory.dto.ProductResponse;
import id.ac.ui.cs.advprog.jsonbackend.inventory.event.InventoryEventPayloadFactory;
import id.ac.ui.cs.advprog.jsonbackend.inventory.event.InventoryEventType;
import id.ac.ui.cs.advprog.jsonbackend.inventory.event.model.InventoryOutboxEvent;
import id.ac.ui.cs.advprog.jsonbackend.inventory.event.repository.InventoryOutboxEventRepository;
import id.ac.ui.cs.advprog.jsonbackend.inventory.exception.ForbiddenInventoryAccessException;
import id.ac.ui.cs.advprog.jsonbackend.inventory.exception.InvalidProductException;
import id.ac.ui.cs.advprog.jsonbackend.inventory.exception.ProductNotFoundException;
import id.ac.ui.cs.advprog.jsonbackend.inventory.mapper.ProductMapper;
import id.ac.ui.cs.advprog.jsonbackend.inventory.model.Product;
import id.ac.ui.cs.advprog.jsonbackend.inventory.repository.ProductRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final InventoryOutboxEventRepository outboxEventRepository;
    private final ProductMapper productMapper;

    public ProductServiceImpl(ProductRepository productRepository,
                              InventoryOutboxEventRepository outboxEventRepository,
                              ProductMapper productMapper) {
        this.productRepository = productRepository;
        this.outboxEventRepository = outboxEventRepository;
        this.productMapper = productMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> findAll() {
        return findAll(0, 20, "createdAt", "desc");
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> findAll(int page, int size, String sortBy, String direction) {
        Pageable pageable = buildPageable(page, size, sortBy, direction);
        return productRepository.findAll(pageable).stream()
                .map(productMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> searchByKeyword(String keyword) {
        return searchByKeyword(keyword, 0, 20, "createdAt", "desc");
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> searchByKeyword(String keyword, int page, int size, String sortBy, String direction) {
        Pageable pageable = buildPageable(page, size, sortBy, direction);
        String normalizedKeyword = keyword == null ? "" : keyword.trim();
        if (normalizedKeyword.isEmpty()) {
            return productRepository.findAll(pageable).stream()
                    .map(productMapper::toResponse)
                    .toList();
        }
        return productRepository
                .findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                        normalizedKeyword, normalizedKeyword, pageable
                )
                .stream()
                .map(productMapper::toResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> findByJastiperId(UUID jastiperId) {
        return findByJastiperId(jastiperId, 0, 20, "createdAt", "desc");
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> findByJastiperId(UUID jastiperId, int page, int size, String sortBy, String direction) {
        if (jastiperId == null) {
            throw new InvalidProductException("Jastiper id is required");
        }
        Pageable pageable = buildPageable(page, size, sortBy, direction);
        return productRepository.findByJastiperId(jastiperId, pageable).stream()
                .map(productMapper::toResponse)
                .toList();
    }

    @Override
    public void reserveStock(UUID productId, int quantity) {
        if (productId == null) {
            throw new InvalidProductException("Product id is required");
        }
        if (quantity <= 0) {
            throw new InvalidProductException("Quantity must be greater than zero");
        }
        Product existing = productRepository.findByIdForUpdate(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + productId));
        existing.reduceStock(quantity);
        productRepository.save(existing);
        appendOutboxEvent(InventoryEventType.STOCK_RESERVED, productId, quantity);
    }

    @Override
    public void releaseStock(UUID productId, int quantity) {
        if (productId == null) {
            throw new InvalidProductException("Product id is required");
        }
        if (quantity <= 0) {
            throw new InvalidProductException("Quantity must be greater than zero");
        }
        Product existing = productRepository.findByIdForUpdate(productId)
                .orElseThrow(() -> new ProductNotFoundException("Product not found with id: " + productId));
        existing.increaseStock(quantity);
        productRepository.save(existing);
        appendOutboxEvent(InventoryEventType.STOCK_RELEASED, productId, quantity);
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
    public ProductResponse createAsJastiper(ProductRequest request, UUID actorId, Role actorRole) {
        assertJastiperActor(actorId, actorRole);
        if (request == null || request.getJastiperId() == null || !actorId.equals(request.getJastiperId())) {
            throw new ForbiddenInventoryAccessException("Jastiper can only create own product");
        }
        return create(request);
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
    public ProductResponse updateAsJastiper(UUID id, ProductRequest request, UUID actorId, Role actorRole) {
        assertJastiperActor(actorId, actorRole);
        Product existing = getProductOrThrow(id);
        if (!actorId.equals(existing.getJastiperId())) {
            throw new ForbiddenInventoryAccessException("Jastiper can only update own product");
        }
        return update(id, request);
    }

    @Override
    public void delete(UUID id) {
        Product existing = getProductOrThrow(id);
        productRepository.delete(existing);
    }

    @Override
    public void deleteAsJastiper(UUID id, UUID actorId, Role actorRole) {
        assertJastiperActor(actorId, actorRole);
        Product existing = getProductOrThrow(id);
        if (!actorId.equals(existing.getJastiperId())) {
            throw new ForbiddenInventoryAccessException("Jastiper can only delete own product");
        }
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

    private void appendOutboxEvent(InventoryEventType eventType, UUID productId, int quantity) {
        InventoryOutboxEvent outboxEvent = InventoryOutboxEvent.builder()
                .eventType(eventType)
                .aggregateId(productId)
                .payload(InventoryEventPayloadFactory.stockMutationPayload(productId, quantity))
                .correlationId(UUID.randomUUID().toString())
                .build();
        outboxEventRepository.save(outboxEvent);
    }

    private void assertJastiperActor(UUID actorId, Role actorRole) {
        if (actorId == null) {
            throw new ForbiddenInventoryAccessException("Authenticated user id is required");
        }
        if (actorRole != Role.JASTIPER) {
            throw new ForbiddenInventoryAccessException("Only jastiper can manage own products");
        }
    }

    private Pageable buildPageable(int page, int size, String sortBy, String direction) {
        if (page < 0) {
            throw new InvalidProductException("Page must be zero or greater");
        }
        if (size <= 0) {
            throw new InvalidProductException("Size must be greater than zero");
        }
        String safeSortBy = (sortBy == null || sortBy.isBlank()) ? "createdAt" : sortBy;
        Sort.Direction sortDirection = Sort.Direction.fromOptionalString(direction).orElse(Sort.Direction.DESC);
        return PageRequest.of(page, size, Sort.by(sortDirection, safeSortBy));
    }
}
