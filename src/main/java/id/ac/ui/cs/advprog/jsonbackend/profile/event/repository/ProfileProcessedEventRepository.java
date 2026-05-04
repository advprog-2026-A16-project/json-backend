package id.ac.ui.cs.advprog.jsonbackend.profile.event.repository;

import id.ac.ui.cs.advprog.jsonbackend.profile.event.model.ProfileProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ProfileProcessedEventRepository extends JpaRepository<ProfileProcessedEvent, UUID> {
    boolean existsByEventIdAndHandlerName(UUID eventId, String handlerName);
}