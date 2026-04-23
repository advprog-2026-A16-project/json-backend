package id.ac.ui.cs.advprog.jsonbackend.inventory.event.repository;

import id.ac.ui.cs.advprog.jsonbackend.inventory.event.model.ProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProcessedEventRepository extends JpaRepository<ProcessedEvent, UUID> {
    boolean existsByEventIdAndHandlerName(UUID eventId, String handlerName);
}
