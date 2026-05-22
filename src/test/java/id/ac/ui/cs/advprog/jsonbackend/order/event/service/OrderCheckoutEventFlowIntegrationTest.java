package id.ac.ui.cs.advprog.jsonbackend.order.event.service;

import id.ac.ui.cs.advprog.jsonbackend.JsonBackendApplication;
import id.ac.ui.cs.advprog.jsonbackend.inventory.model.Product;
import id.ac.ui.cs.advprog.jsonbackend.inventory.repository.ProductRepository;
import id.ac.ui.cs.advprog.jsonbackend.order.dto.OrderRequest;
import id.ac.ui.cs.advprog.jsonbackend.order.dto.OrderResponse;
import id.ac.ui.cs.advprog.jsonbackend.order.event.OutboxEventStatus;
import id.ac.ui.cs.advprog.jsonbackend.order.event.repository.OrderOutboxEventRepository;
import id.ac.ui.cs.advprog.jsonbackend.order.event.repository.OrderProcessedEventRepository;
import id.ac.ui.cs.advprog.jsonbackend.order.model.Order;
import id.ac.ui.cs.advprog.jsonbackend.order.model.OrderStatus;
import id.ac.ui.cs.advprog.jsonbackend.order.repository.OrderRepository;
import id.ac.ui.cs.advprog.jsonbackend.order.service.OrderService;
import id.ac.ui.cs.advprog.jsonbackend.wallet.event.repository.WalletOutboxEventRepository;
import id.ac.ui.cs.advprog.jsonbackend.wallet.event.service.WalletOutboxEventDispatcher;
import id.ac.ui.cs.advprog.jsonbackend.wallet.model.Wallet;
import id.ac.ui.cs.advprog.jsonbackend.wallet.repository.TransactionRepository;
import id.ac.ui.cs.advprog.jsonbackend.wallet.repository.WalletRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(classes = JsonBackendApplication.class)
@ActiveProfiles("test")
class OrderCheckoutEventFlowIntegrationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    @Qualifier("orderOutboxEventDispatcher")
    private OutboxEventDispatcher orderDispatcher;

    @Autowired
    private WalletOutboxEventDispatcher walletDispatcher;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderOutboxEventRepository orderOutboxEventRepository;

    @Autowired
    private OrderProcessedEventRepository orderProcessedEventRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private WalletOutboxEventRepository walletOutboxEventRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @AfterEach
    void tearDown() {
        walletOutboxEventRepository.deleteAll();
        orderProcessedEventRepository.deleteAll();
        orderOutboxEventRepository.deleteAll();
        transactionRepository.deleteAll();
        orderRepository.deleteAll();
        walletRepository.deleteAll();
        productRepository.deleteAll();
    }

    @Test
    void checkoutEventFlowShouldReserveStockBeforeChargingWallet() {
        UUID titipersId = UUID.randomUUID();
        Product product = productRepository.save(productWithStock(5));
        walletRepository.save(walletWithBalance(titipersId, new BigDecimal("200000")));

        OrderResponse response = orderService.create(orderRequest(product.getId(), titipersId, 2));

        orderDispatcher.dispatchPendingEvents();
        walletDispatcher.dispatchPendingEvents();

        Product updatedProduct = productRepository.findById(product.getId()).orElseThrow();
        Wallet updatedWallet = walletRepository.findByUserId(titipersId).orElseThrow();
        Order updatedOrder = orderRepository.findById(response.getId()).orElseThrow();

        assertEquals(3, updatedProduct.getStock());
        assertEquals(new BigDecimal("100000.00"), updatedWallet.getBalance());
        assertEquals(OrderStatus.PAID, updatedOrder.getStatus());
        assertEquals(1, transactionRepository.findByUserIdOrderByCreatedAtDesc(titipersId).size());
        assertEquals(1, walletOutboxEventRepository.count());
    }

    @Test
    void checkoutEventFlowShouldCancelOrderWithoutChargingWalletWhenReservationFailsLater() {
        UUID titipersId = UUID.randomUUID();
        Product product = productRepository.save(productWithStock(2));
        walletRepository.save(walletWithBalance(titipersId, new BigDecimal("200000")));

        OrderResponse response = orderService.create(orderRequest(product.getId(), titipersId, 2));
        product.setStock(0);
        productRepository.save(product);

        orderDispatcher.dispatchPendingEvents();

        Product updatedProduct = productRepository.findById(product.getId()).orElseThrow();
        Wallet updatedWallet = walletRepository.findByUserId(titipersId).orElseThrow();
        Order updatedOrder = orderRepository.findById(response.getId()).orElseThrow();

        assertEquals(0, updatedProduct.getStock());
        assertEquals(new BigDecimal("200000.00"), updatedWallet.getBalance());
        assertEquals(OrderStatus.CANCELLED, updatedOrder.getStatus());
        assertEquals(0, transactionRepository.findByUserIdOrderByCreatedAtDesc(titipersId).size());
        assertEquals(0, walletOutboxEventRepository.count());
        assertEquals(OutboxEventStatus.FAILED, orderOutboxEventRepository.findAll().getFirst().getStatus());
    }

    private Product productWithStock(int stock) {
        return Product.builder()
                .name("Checkout Product")
                .description("Checkout event flow")
                .price(new BigDecimal("50000.00"))
                .stock(stock)
                .originCountry("JP")
                .purchaseDate(LocalDate.now().plusDays(1))
                .jastiperId(UUID.randomUUID())
                .build();
    }

    private Wallet walletWithBalance(UUID userId, BigDecimal balance) {
        Wallet wallet = new Wallet();
        wallet.setUserId(userId);
        wallet.setBalance(balance);
        return wallet;
    }

    private OrderRequest orderRequest(UUID productId, UUID titipersId, int quantity) {
        OrderRequest request = new OrderRequest();
        request.setProductId(productId);
        request.setTitipersId(titipersId);
        request.setQuantity(quantity);
        request.setShippingAddress("Fasilkom UI, Depok");
        return request;
    }
}
