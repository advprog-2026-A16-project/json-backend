package id.ac.ui.cs.advprog.jsonbackend.order.event.repository;

import id.ac.ui.cs.advprog.jsonbackend.order.event.model.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, UUID> {

    // Fungsi ini wajib ditambahkan untuk validasi idempotency di Handler nanti
    boolean existsByEventIdAndHandlerName(UUID eventId, String handlerName);
}