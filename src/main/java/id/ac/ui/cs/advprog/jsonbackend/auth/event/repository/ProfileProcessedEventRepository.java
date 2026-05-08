package id.ac.ui.cs.advprog.jsonbackend.auth.event.repository;

import id.ac.ui.cs.advprog.jsonbackend.auth.event.model.AuthProfileProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ProfileProcessedEventRepository extends JpaRepository<AuthProfileProcessedEvent, UUID> {
    boolean existsByEventIdAndHandlerName(UUID eventId, String handlerName);
}