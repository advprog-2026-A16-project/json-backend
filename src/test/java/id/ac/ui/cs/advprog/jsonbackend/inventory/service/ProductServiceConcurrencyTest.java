package id.ac.ui.cs.advprog.jsonbackend.inventory.service;

import id.ac.ui.cs.advprog.jsonbackend.inventory.exception.InsufficientStockException;
import id.ac.ui.cs.advprog.jsonbackend.inventory.model.Product;
import id.ac.ui.cs.advprog.jsonbackend.inventory.repository.ProductRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
class ProductServiceConcurrencyTest {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;

    @AfterEach
    void tearDown() {
        productRepository.deleteAll();
    }

    @Test
    void reserveStockConcurrentRequestsShouldPreventOverselling() throws Exception {
        Product product = Product.builder()
                .name("War Product")
                .description("Limited stock")
                .price(new BigDecimal("100000"))
                .stock(1)
                .originCountry("JP")
                .purchaseDate(LocalDate.now().plusDays(3))
                .jastiperId(UUID.randomUUID())
                .build();
        Product saved = productRepository.save(product);

        int workerCount = 2;
        ExecutorService executorService = Executors.newFixedThreadPool(workerCount);
        CountDownLatch readyLatch = new CountDownLatch(workerCount);
        CountDownLatch startLatch = new CountDownLatch(1);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger insufficientCount = new AtomicInteger(0);

        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < workerCount; i++) {
            futures.add(executorService.submit(() -> {
                readyLatch.countDown();
                startLatch.await();
                try {
                    productService.reserveStock(saved.getId(), 1);
                    successCount.incrementAndGet();
                } catch (InsufficientStockException ex) {
                    insufficientCount.incrementAndGet();
                }
                return null;
            }));
        }

        readyLatch.await();
        startLatch.countDown();

        for (Future<?> future : futures) {
            future.get();
        }
        executorService.shutdown();

        Product updated = productRepository.findById(saved.getId()).orElseThrow();
        assertEquals(0, updated.getStock());
        assertEquals(1, successCount.get());
        assertEquals(1, insufficientCount.get());
    }

    @Test
    void reserveStockManyConcurrentRequestsShouldNeverMakeStockNegative() throws Exception {
        Product product = Product.builder()
                .name("War Product Burst")
                .description("Burst stock")
                .price(new BigDecimal("200000"))
                .stock(5)
                .originCountry("KR")
                .purchaseDate(LocalDate.now().plusDays(5))
                .jastiperId(UUID.randomUUID())
                .build();
        Product saved = productRepository.save(product);

        int workerCount = 10;
        ExecutorService executorService = Executors.newFixedThreadPool(workerCount);
        CountDownLatch readyLatch = new CountDownLatch(workerCount);
        CountDownLatch startLatch = new CountDownLatch(1);

        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger insufficientCount = new AtomicInteger(0);

        List<Future<?>> futures = new ArrayList<>();
        for (int i = 0; i < workerCount; i++) {
            futures.add(executorService.submit(() -> {
                readyLatch.countDown();
                startLatch.await();
                try {
                    productService.reserveStock(saved.getId(), 1);
                    successCount.incrementAndGet();
                } catch (InsufficientStockException ex) {
                    insufficientCount.incrementAndGet();
                }
                return null;
            }));
        }

        readyLatch.await();
        startLatch.countDown();

        for (Future<?> future : futures) {
            future.get();
        }
        executorService.shutdown();

        Product updated = productRepository.findById(saved.getId()).orElseThrow();
        assertEquals(0, updated.getStock());
        assertEquals(5, successCount.get());
        assertEquals(5, insufficientCount.get());
    }
}
