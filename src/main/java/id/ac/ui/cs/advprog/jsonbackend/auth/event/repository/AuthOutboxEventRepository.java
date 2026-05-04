package id.ac.ui.cs.advprog.jsonbackend.auth.event.repository;

import id.ac.ui.cs.advprog.jsonbackend.auth.event.enums.AuthOutboxEventStatus;
import id.ac.ui.cs.advprog.jsonbackend.auth.event.model.AuthOutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface AuthOutboxEventRepository extends JpaRepository<AuthOutboxEvent, UUID> {
    List<AuthOutboxEvent> findTop50ByStatusOrderByCreatedAtAsc(AuthOutboxEventStatus status);
}