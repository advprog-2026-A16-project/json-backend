package id.ac.ui.cs.advprog.jsonbackend.order.repository;

import id.ac.ui.cs.advprog.jsonbackend.order.model.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderRepository extends JpaRepository<Order, UUID> {

    // Untuk Titipers: Melihat riwayat belanja mereka
    List<Order> findByTitipersId(UUID titipersId);

    // Untuk Jastiper: Melihat daftar pesanan yang masuk (To-Do List)
    List<Order> findByJastiperId(UUID jastiperId);

}