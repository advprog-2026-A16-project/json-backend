package id.ac.ui.cs.advprog.jsonbackend.order.service;

import id.ac.ui.cs.advprog.jsonbackend.inventory.dto.ProductResponse;
import id.ac.ui.cs.advprog.jsonbackend.inventory.exception.ProductNotFoundException;
import id.ac.ui.cs.advprog.jsonbackend.inventory.service.ProductService;
import id.ac.ui.cs.advprog.jsonbackend.order.dto.OrderRatingRequest;
import id.ac.ui.cs.advprog.jsonbackend.order.dto.OrderRequest;
import id.ac.ui.cs.advprog.jsonbackend.order.dto.OrderResponse;
import id.ac.ui.cs.advprog.jsonbackend.order.dto.OrderStatusUpdateRequest;
import id.ac.ui.cs.advprog.jsonbackend.order.exception.InvalidOrderException;
import id.ac.ui.cs.advprog.jsonbackend.order.exception.OrderNotFoundException;
import id.ac.ui.cs.advprog.jsonbackend.order.mapper.OrderMapper;
import id.ac.ui.cs.advprog.jsonbackend.order.model.Order;
import id.ac.ui.cs.advprog.jsonbackend.order.model.OrderStatus;
import id.ac.ui.cs.advprog.jsonbackend.order.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderMapper orderMapper;
    private final ProductService productService;

    public OrderServiceImpl(OrderRepository orderRepository, OrderMapper orderMapper, ProductService productService) {
        this.orderRepository = orderRepository;
        this.orderMapper = orderMapper;
        this.productService = productService;
    }

    @Override
    public OrderResponse create(OrderRequest request) {
        // Ambil detail produk dari Modul Inventory pake fungsi yang udah ada
        ProductResponse product = productService.findById(request.getProductId());

        // Verifikasi stok dari response yang didapat
        if (product.getStock() < request.getQuantity()) {
            throw new InvalidOrderException("Stok tidak mencukupi untuk pesanan ini");
        }

        // Kalkulasi harga asli berdasarkan harga dari Modul Inventory
        BigDecimal totalPrice = product.getPrice().multiply(BigDecimal.valueOf(request.getQuantity()));

        // TODO: (Wallet) Panggil WalletService untuk potong saldo

        // TODO: (Inventory) Kurang stok
        productService.reserveStock(product.getId(), request.getQuantity());

        // Rakit data pesanan
        Order order = orderMapper.toEntity(request);

        // Otomatis set jastiperId pakai data yang ditarik dari produk
        order.setJastiperId(product.getJastiperId());
        order.setTotalPrice(totalPrice);
        order.setStatus(OrderStatus.PAID);

        // Simpan ke database Order
        Order saved = orderRepository.save(order);
        return orderMapper.toResponse(saved);
    }

    // Ambil semua data pesanan
    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> findAll() {
        return orderRepository.findAll().stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    // Cari pesanan berdasarkan ID
    @Override
    @Transactional(readOnly = true)
    public OrderResponse findById(UUID id) {
        Order order = getOrderOrThrow(id);
        return orderMapper.toResponse(order);
    }

    // Ambil riwayat pesanan milik Titiper
    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> findByTitipersId(UUID titipersId) {
        return orderRepository.findByTitipersId(titipersId).stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    // Ambil daftar pesanan yang harus diproses Jastiper
    @Override
    @Transactional(readOnly = true)
    public List<OrderResponse> findByJastiperId(UUID jastiperId) {
        return orderRepository.findByJastiperId(jastiperId).stream()
                .map(orderMapper::toResponse)
                .toList();
    }

    // Placeholder fungsi lainnya
    @Override public OrderResponse updateStatus(UUID id, OrderStatusUpdateRequest request) { return null; }
    @Override public OrderResponse cancelByJastiper(UUID id) { return null; }
    @Override public OrderResponse giveRating(UUID id, OrderRatingRequest request) { return null; }

    // Helper untuk cari pesanan atau lempar error
    private Order getOrderOrThrow(UUID id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new OrderNotFoundException("Order not found with id: " + id));
    }
}