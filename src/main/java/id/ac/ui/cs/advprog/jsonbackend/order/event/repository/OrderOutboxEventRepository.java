package id.ac.ui.cs.advprog.jsonbackend.order.event.repository;

import id.ac.ui.cs.advprog.jsonbackend.order.event.model.OrderOutboxEvent;
import id.ac.ui.cs.advprog.jsonbackend.order.event.OutboxEventStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface OrderOutboxEventRepository extends JpaRepository<OrderOutboxEvent, UUID> {
    List<OrderOutboxEvent> findByStatusOrderByCreatedAtAsc(OutboxEventStatus status);
}