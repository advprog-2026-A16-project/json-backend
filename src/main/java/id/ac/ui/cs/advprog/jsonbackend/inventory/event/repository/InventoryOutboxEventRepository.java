package id.ac.ui.cs.advprog.jsonbackend.inventory.event.repository;

import id.ac.ui.cs.advprog.jsonbackend.inventory.event.OutboxEventStatus;
import id.ac.ui.cs.advprog.jsonbackend.inventory.event.model.InventoryOutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface InventoryOutboxEventRepository extends JpaRepository<InventoryOutboxEvent, UUID> {
    List<InventoryOutboxEvent> findTop50ByStatusOrderByCreatedAtAsc(OutboxEventStatus status);

    List<InventoryOutboxEvent> findTop50ByStatusOrderByOccurredAtAsc(OutboxEventStatus status);
}
