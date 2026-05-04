package id.ac.ui.cs.advprog.jsonbackend.inventory.service;

import id.ac.ui.cs.advprog.jsonbackend.auth.enums.Role;
import id.ac.ui.cs.advprog.jsonbackend.inventory.dto.ProductRequest;
import id.ac.ui.cs.advprog.jsonbackend.inventory.dto.ProductResponse;
import id.ac.ui.cs.advprog.jsonbackend.inventory.event.InventoryEventType;
import id.ac.ui.cs.advprog.jsonbackend.inventory.event.model.InventoryOutboxEvent;
import id.ac.ui.cs.advprog.jsonbackend.inventory.event.repository.InventoryOutboxEventRepository;
import id.ac.ui.cs.advprog.jsonbackend.inventory.exception.ForbiddenInventoryAccessException;
import id.ac.ui.cs.advprog.jsonbackend.inventory.exception.InvalidProductException;
import id.ac.ui.cs.advprog.jsonbackend.inventory.exception.InsufficientStockException;
import id.ac.ui.cs.advprog.jsonbackend.inventory.exception.ProductNotFoundException;
import id.ac.ui.cs.advprog.jsonbackend.inventory.mapper.ProductMapper;
import id.ac.ui.cs.advprog.jsonbackend.inventory.model.Product;
import id.ac.ui.cs.advprog.jsonbackend.inventory.repository.ProductRepository;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @Mock
    private InventoryOutboxEventRepository outboxEventRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void createProductSuccess() {
        ProductRequest request = sampleRequest();
        Product entity = sampleEntity();
        ProductResponse response = sampleResponse(entity.getId());

        when(productMapper.toEntity(request)).thenReturn(entity);
        when(productRepository.save(entity)).thenReturn(entity);
        when(productMapper.toResponse(entity)).thenReturn(response);

        ProductResponse result = productService.create(request);

        assertEquals(response.getId(), result.getId());
        verify(productMapper, times(1)).toEntity(request);
        verify(productRepository, times(1)).save(entity);
        verify(productMapper, times(1)).toResponse(entity);
    }

    @Test
    void createProductThrowsWhenPriceInvalid() {
        ProductRequest request = sampleRequest();
        request.setPrice(BigDecimal.ZERO);

        assertThrows(InvalidProductException.class, () -> productService.create(request));

        verify(productMapper, never()).toEntity(request);
        verify(productRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void createProductThrowsWhenJastiperIdMissing() {
        ProductRequest request = sampleRequest();
        request.setJastiperId(null);

        assertThrows(InvalidProductException.class, () -> productService.create(request));

        verify(productMapper, never()).toEntity(request);
        verify(productRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void createProductThrowsWhenRequestNull() {
        assertThrows(InvalidProductException.class, () -> productService.create(null));
        verify(productMapper, never()).toEntity(org.mockito.ArgumentMatchers.any());
        verify(productRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void createAsJastiperThrowsWhenActorNotJastiper() {
        ProductRequest request = sampleRequest();
        UUID actorId = request.getJastiperId();

        assertThrows(ForbiddenInventoryAccessException.class,
                () -> productService.createAsJastiper(request, actorId, Role.TITIPERS));
        verify(productRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void createAsJastiperThrowsWhenActorNotOwner() {
        ProductRequest request = sampleRequest();
        UUID actorId = UUID.randomUUID();

        assertThrows(ForbiddenInventoryAccessException.class,
                () -> productService.createAsJastiper(request, actorId, Role.JASTIPER));
        verify(productRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void findAllProductsSuccess() {
        Product entity = sampleEntity();
        ProductResponse response = sampleResponse(entity.getId());
        Pageable pageable = PageRequest.of(0, 20, org.springframework.data.domain.Sort.by(
                org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));

        when(productRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(entity)));
        when(productMapper.toResponse(entity)).thenReturn(response);

        List<ProductResponse> result = productService.findAll();

        assertEquals(1, result.size());
        assertEquals(response.getId(), result.getFirst().getId());
        verify(productRepository, times(1)).findAll(pageable);
    }

    @Test
    void searchProductsByKeywordSuccess() {
        String keyword = "sneakers";
        Product entity = sampleEntity();
        ProductResponse response = sampleResponse(entity.getId());
        Pageable pageable = PageRequest.of(0, 20, org.springframework.data.domain.Sort.by(
                org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));

        when(productRepository.findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword, pageable))
                .thenReturn(new PageImpl<>(List.of(entity)));
        when(productMapper.toResponse(entity)).thenReturn(response);

        List<ProductResponse> result = productService.searchByKeyword(keyword);

        assertEquals(1, result.size());
        assertEquals(response.getId(), result.getFirst().getId());
        verify(productRepository, times(1))
                .findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(keyword, keyword, pageable);
        verify(productMapper, times(1)).toResponse(entity);
    }

    @Test
    void searchProductsByKeywordBlankShouldFallbackToFindAll() {
        String keyword = "   ";
        Product entity = sampleEntity();
        ProductResponse response = sampleResponse(entity.getId());
        Pageable pageable = PageRequest.of(0, 20, org.springframework.data.domain.Sort.by(
                org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));

        when(productRepository.findAll(pageable)).thenReturn(new PageImpl<>(List.of(entity)));
        when(productMapper.toResponse(entity)).thenReturn(response);

        List<ProductResponse> result = productService.searchByKeyword(keyword);

        assertEquals(1, result.size());
        assertEquals(response.getId(), result.getFirst().getId());
        verify(productRepository, times(1)).findAll(pageable);
        verify(productRepository, never())
                .findByNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(org.mockito.ArgumentMatchers.anyString(),
                        org.mockito.ArgumentMatchers.anyString(),
                        org.mockito.ArgumentMatchers.any(Pageable.class));
        verify(productMapper, times(1)).toResponse(entity);
    }

    @Test
    void findProductsByJastiperIdSuccess() {
        UUID jastiperId = UUID.randomUUID();
        Product entity = sampleEntity();
        ProductResponse response = sampleResponse(entity.getId());
        Pageable pageable = PageRequest.of(0, 20, org.springframework.data.domain.Sort.by(
                org.springframework.data.domain.Sort.Direction.DESC, "createdAt"));

        when(productRepository.findByJastiperId(jastiperId, pageable)).thenReturn(new PageImpl<>(List.of(entity)));
        when(productMapper.toResponse(entity)).thenReturn(response);

        List<ProductResponse> result = productService.findByJastiperId(jastiperId);

        assertEquals(1, result.size());
        assertEquals(response.getId(), result.getFirst().getId());
        verify(productRepository, times(1)).findByJastiperId(jastiperId, pageable);
        verify(productMapper, times(1)).toResponse(entity);
    }

    @Test
    void findProductsByJastiperIdThrowsWhenNull() {
        assertThrows(InvalidProductException.class, () -> productService.findByJastiperId(null));
        verify(productRepository, never()).findByJastiperId(org.mockito.ArgumentMatchers.any(), org.mockito.ArgumentMatchers.any(Pageable.class));
    }

    @Test
    void findAllThrowsWhenSortByFieldIsUnsupported() {
        assertThrows(InvalidProductException.class,
                () -> productService.findAll(0, 20, "dropTable", "desc"));
        verify(productRepository, never()).findAll(org.mockito.ArgumentMatchers.any(Pageable.class));
    }

    @Test
    void findAllThrowsWhenDirectionInvalid() {
        when(productRepository.findAll(org.mockito.ArgumentMatchers.any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of()));
        assertThrows(InvalidProductException.class,
                () -> productService.findAll(0, 20, "createdAt", "sideways"));
        verify(productRepository, never()).findAll(org.mockito.ArgumentMatchers.any(Pageable.class));
    }

    @Test
    void reserveStockSuccess() {
        UUID productId = UUID.randomUUID();
        Product existing = sampleEntity();
        existing.setStock(10);

        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(existing));
        when(productRepository.save(existing)).thenReturn(existing);

        productService.reserveStock(productId, 3);

        assertEquals(7, existing.getStock());
        verify(productRepository, times(1)).findByIdForUpdate(productId);
        verify(productRepository, never()).findById(productId);
        verify(productRepository, times(1)).save(existing);
        verify(outboxEventRepository, times(1)).save(org.mockito.ArgumentMatchers.any(InventoryOutboxEvent.class));
    }

    @Test
    void reserveStockThrowsWhenInsufficient() {
        UUID productId = UUID.randomUUID();
        Product existing = sampleEntity();
        existing.setStock(2);

        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(existing));

        assertThrows(InsufficientStockException.class, () -> productService.reserveStock(productId, 3));
        verify(productRepository, times(1)).findByIdForUpdate(productId);
        verify(productRepository, never()).findById(productId);
        verify(productRepository, never()).save(existing);
        verify(outboxEventRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void reserveStockThrowsWhenQuantityNotPositive() {
        UUID productId = UUID.randomUUID();

        assertThrows(InvalidProductException.class, () -> productService.reserveStock(productId, 0));
        verify(productRepository, never()).findByIdForUpdate(org.mockito.ArgumentMatchers.any());
        verify(productRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(outboxEventRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void reserveStockThrowsWhenProductIdNull() {
        assertThrows(InvalidProductException.class, () -> productService.reserveStock(null, 1));
        verify(productRepository, never()).findByIdForUpdate(org.mockito.ArgumentMatchers.any());
        verify(productRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(outboxEventRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void reserveStockThrowsWhenProductNotFound() {
        UUID productId = UUID.randomUUID();
        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.reserveStock(productId, 1));
        verify(productRepository, times(1)).findByIdForUpdate(productId);
        verify(productRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(outboxEventRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void releaseStockSuccess() {
        UUID productId = UUID.randomUUID();
        Product existing = sampleEntity();
        existing.setId(productId);
        existing.setStock(2);

        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.of(existing));
        when(productRepository.save(existing)).thenReturn(existing);

        productService.releaseStock(productId, 3);

        assertEquals(5, existing.getStock());
        verify(productRepository, times(1)).findByIdForUpdate(productId);
        verify(productRepository, times(1)).save(existing);

        ArgumentCaptor<InventoryOutboxEvent> captor = ArgumentCaptor.forClass(InventoryOutboxEvent.class);
        verify(outboxEventRepository, times(1)).save(captor.capture());
        assertEquals(InventoryEventType.STOCK_RELEASED, captor.getValue().getEventType());
        assertEquals(productId, captor.getValue().getAggregateId());
    }

    @Test
    void releaseStockThrowsWhenQuantityNotPositive() {
        UUID productId = UUID.randomUUID();

        assertThrows(InvalidProductException.class, () -> productService.releaseStock(productId, 0));
        verify(productRepository, never()).findByIdForUpdate(org.mockito.ArgumentMatchers.any());
        verify(productRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(outboxEventRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void releaseStockThrowsWhenProductNotFound() {
        UUID productId = UUID.randomUUID();
        when(productRepository.findByIdForUpdate(productId)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.releaseStock(productId, 1));
        verify(productRepository, times(1)).findByIdForUpdate(productId);
        verify(productRepository, never()).save(org.mockito.ArgumentMatchers.any());
        verify(outboxEventRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void findByIdThrowsWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class, () -> productService.findById(id));
        verify(productRepository, times(1)).findById(id);
    }

    @Test
    void updateProductSuccess() {
        UUID id = UUID.randomUUID();
        ProductRequest request = sampleRequest();
        Product existing = sampleEntity();
        ProductResponse response = sampleResponse(id);

        when(productRepository.findById(id)).thenReturn(Optional.of(existing));
        when(productRepository.save(existing)).thenReturn(existing);
        when(productMapper.toResponse(existing)).thenReturn(response);

        ProductResponse result = productService.update(id, request);

        assertEquals(id, result.getId());
        verify(productMapper, times(1)).updateEntity(existing, request);
        verify(productRepository, times(1)).save(existing);
    }

    @Test
    void updateAsJastiperThrowsWhenNotOwner() {
        UUID id = UUID.randomUUID();
        ProductRequest request = sampleRequest();
        Product existing = sampleEntity();
        existing.setJastiperId(UUID.randomUUID());

        when(productRepository.findById(id)).thenReturn(Optional.of(existing));

        assertThrows(ForbiddenInventoryAccessException.class,
                () -> productService.updateAsJastiper(id, request, UUID.randomUUID(), Role.JASTIPER));
        verify(productRepository, never()).save(existing);
    }

    @Test
    void updateAsJastiperThrowsWhenActorNotJastiper() {
        UUID id = UUID.randomUUID();
        ProductRequest request = sampleRequest();

        assertThrows(ForbiddenInventoryAccessException.class,
                () -> productService.updateAsJastiper(id, request, UUID.randomUUID(), Role.TITIPERS));
        verify(productRepository, never()).findById(id);
        verify(productRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }

    @Test
    void deleteProductSuccess() {
        UUID id = UUID.randomUUID();
        Product existing = sampleEntity();

        when(productRepository.findById(id)).thenReturn(Optional.of(existing));

        productService.delete(id);

        verify(productRepository, times(1)).delete(existing);
    }

    @Test
    void deleteAsJastiperThrowsWhenNotOwner() {
        UUID id = UUID.randomUUID();
        Product existing = sampleEntity();
        existing.setJastiperId(UUID.randomUUID());

        when(productRepository.findById(id)).thenReturn(Optional.of(existing));

        assertThrows(ForbiddenInventoryAccessException.class,
                () -> productService.deleteAsJastiper(id, UUID.randomUUID(), Role.JASTIPER));
        verify(productRepository, never()).delete(existing);
    }

    @Test
    void deleteAsJastiperThrowsWhenActorNotJastiper() {
        UUID id = UUID.randomUUID();

        assertThrows(ForbiddenInventoryAccessException.class,
                () -> productService.deleteAsJastiper(id, UUID.randomUUID(), Role.TITIPERS));
        verify(productRepository, never()).findById(id);
        verify(productRepository, never()).delete(org.mockito.ArgumentMatchers.any());
    }

    private ProductRequest sampleRequest() {
        return ProductRequest.builder()
                .name("Sneakers Limited Edition")
                .description("Beli di US")
                .price(new BigDecimal("1500000"))
                .stock(10)
                .originCountry("US")
                .purchaseDate(LocalDate.now().plusDays(7))
                .jastiperId(UUID.randomUUID())
                .build();
    }

    private Product sampleEntity() {
        UUID id = UUID.randomUUID();
        LocalDateTime now = LocalDateTime.now();
        return Product.builder()
                .id(id)
                .name("Sneakers Limited Edition")
                .description("Beli di US")
                .price(new BigDecimal("1500000"))
                .stock(10)
                .originCountry("US")
                .purchaseDate(LocalDate.now().plusDays(7))
                .jastiperId(UUID.randomUUID())
                .createdAt(now)
                .updatedAt(now)
                .build();
    }

    private ProductResponse sampleResponse(UUID id) {
        LocalDateTime now = LocalDateTime.now();
        return ProductResponse.builder()
                .id(id)
                .name("Sneakers Limited Edition")
                .description("Beli di US")
                .price(new BigDecimal("1500000"))
                .stock(10)
                .originCountry("US")
                .purchaseDate(LocalDate.now().plusDays(7))
                .jastiperId(UUID.randomUUID())
                .createdAt(now)
                .updatedAt(now)
                .build();
    }
}
