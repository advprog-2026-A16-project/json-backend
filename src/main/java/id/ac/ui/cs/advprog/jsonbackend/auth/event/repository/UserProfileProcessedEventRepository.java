package id.ac.ui.cs.advprog.jsonbackend.auth.event.repository;

import id.ac.ui.cs.advprog.jsonbackend.auth.event.model.UserProfileProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface UserProfileProcessedEventRepository extends JpaRepository<UserProfileProcessedEvent, UUID> {
    boolean existsByEventIdAndHandlerName(UUID eventId, String handlerName);
}