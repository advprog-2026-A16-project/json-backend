package id.ac.ui.cs.advprog.jsonbackend.inventory.event.service;

import id.ac.ui.cs.advprog.jsonbackend.JsonBackendApplication;
import id.ac.ui.cs.advprog.jsonbackend.inventory.event.InventoryEventPayloadFactory;
import id.ac.ui.cs.advprog.jsonbackend.inventory.event.InventoryEventType;
import id.ac.ui.cs.advprog.jsonbackend.inventory.event.OutboxEventStatus;
import id.ac.ui.cs.advprog.jsonbackend.inventory.event.model.InventoryOutboxEvent;
import id.ac.ui.cs.advprog.jsonbackend.inventory.event.repository.InventoryOutboxEventRepository;
import id.ac.ui.cs.advprog.jsonbackend.inventory.event.repository.ProcessedEventRepository;
import id.ac.ui.cs.advprog.jsonbackend.inventory.model.Product;
import id.ac.ui.cs.advprog.jsonbackend.inventory.repository.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(classes = JsonBackendApplication.class)
@ActiveProfiles("test")
class InventoryEventFlowIntegrationTest {

    @org.springframework.beans.factory.annotation.Autowired
    private ProductRepository productRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private InventoryOutboxEventRepository outboxEventRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private ProcessedEventRepository processedEventRepository;

    @org.springframework.beans.factory.annotation.Autowired
    private OutboxEventDispatcher dispatcher;

    @AfterEach
    void tearDown() {
        processedEventRepository.deleteAll();
        outboxEventRepository.deleteAll();
        productRepository.deleteAll();
    }

    @Test
    void dispatchPendingEventsShouldReserveStockAndMarkProcessed() {
        Product product = Product.builder()
                .name("Flow Product")
                .description("Flow desc")
                .price(new BigDecimal("100000"))
                .stock(5)
                .originCountry("JP")
                .purchaseDate(LocalDate.now().plusDays(1))
                .jastiperId(UUID.randomUUID())
                .build();
        Product savedProduct = productRepository.save(product);

        UUID eventId = UUID.randomUUID();
        InventoryOutboxEvent outboxEvent = InventoryOutboxEvent.builder()
                .eventId(eventId)
                .eventType(InventoryEventType.STOCK_RESERVED)
                .aggregateId(savedProduct.getId())
                .payload(InventoryEventPayloadFactory.stockMutationPayload(savedProduct.getId(), 2))
                .correlationId("corr-integration-1")
                .status(OutboxEventStatus.PENDING)
                .retryCount(0)
                .build();
        InventoryOutboxEvent savedEvent = outboxEventRepository.save(outboxEvent);

        dispatcher.dispatchPendingEvents();

        Product updated = productRepository.findById(savedProduct.getId()).orElseThrow();
        assertEquals(3, updated.getStock());

        InventoryOutboxEvent updatedOutbox = outboxEventRepository.findById(savedEvent.getId()).orElseThrow();
        assertEquals(OutboxEventStatus.SENT, updatedOutbox.getStatus());
        assertTrue(processedEventRepository
                .existsByEventIdAndHandlerName(eventId, "StockReservationRequestedEventHandler"));
    }

    @Test
    void dispatchPendingEventsShouldReleaseStockAndMarkProcessed() {
        Product product = Product.builder()
                .name("Flow Product Release")
                .description("Flow release desc")
                .price(new BigDecimal("100000"))
                .stock(3)
                .originCountry("JP")
                .purchaseDate(LocalDate.now().plusDays(1))
                .jastiperId(UUID.randomUUID())
                .build();
        Product savedProduct = productRepository.save(product);

        UUID eventId = UUID.randomUUID();
        InventoryOutboxEvent outboxEvent = InventoryOutboxEvent.builder()
                .eventId(eventId)
                .eventType(InventoryEventType.STOCK_RELEASED)
                .aggregateId(savedProduct.getId())
                .payload(InventoryEventPayloadFactory.stockMutationPayload(savedProduct.getId(), 2))
                .correlationId("corr-integration-release-1")
                .status(OutboxEventStatus.PENDING)
                .retryCount(0)
                .build();
        InventoryOutboxEvent savedEvent = outboxEventRepository.save(outboxEvent);

        dispatcher.dispatchPendingEvents();

        Product updated = productRepository.findById(savedProduct.getId()).orElseThrow();
        assertEquals(5, updated.getStock());

        InventoryOutboxEvent updatedOutbox = outboxEventRepository.findById(savedEvent.getId()).orElseThrow();
        assertEquals(OutboxEventStatus.SENT, updatedOutbox.getStatus());
        assertTrue(processedEventRepository
                .existsByEventIdAndHandlerName(eventId, "StockReleaseRequestedEventHandler"));
    }

    @Test
    void dispatchPendingEventsShouldMoveUnsupportedEventTypeToDeadLetter() {
        Product product = Product.builder()
                .name("Flow Unsupported")
                .description("Flow unsupported desc")
                .price(new BigDecimal("100000"))
                .stock(3)
                .originCountry("JP")
                .purchaseDate(LocalDate.now().plusDays(1))
                .jastiperId(UUID.randomUUID())
                .build();
        Product savedProduct = productRepository.save(product);

        UUID eventId = UUID.randomUUID();
        InventoryOutboxEvent outboxEvent = InventoryOutboxEvent.builder()
                .eventId(eventId)
                .eventType(InventoryEventType.STOCK_RESERVATION_FAILED)
                .aggregateId(savedProduct.getId())
                .payload(InventoryEventPayloadFactory.stockMutationPayload(savedProduct.getId(), 1))
                .correlationId("corr-integration-unsupported-1")
                .status(OutboxEventStatus.PENDING)
                .retryCount(0)
                .build();
        InventoryOutboxEvent savedEvent = outboxEventRepository.save(outboxEvent);

        dispatcher.dispatchPendingEvents();

        Product updated = productRepository.findById(savedProduct.getId()).orElseThrow();
        assertEquals(3, updated.getStock());

        InventoryOutboxEvent updatedOutbox = outboxEventRepository.findById(savedEvent.getId()).orElseThrow();
        assertEquals(OutboxEventStatus.DEAD_LETTER, updatedOutbox.getStatus());
        assertEquals(0, updatedOutbox.getRetryCount());
    }
}
