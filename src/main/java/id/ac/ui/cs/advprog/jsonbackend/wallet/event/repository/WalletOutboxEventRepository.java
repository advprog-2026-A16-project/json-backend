package id.ac.ui.cs.advprog.jsonbackend.wallet.event.repository;

import id.ac.ui.cs.advprog.jsonbackend.wallet.event.WalletOutboxEventStatus;
import id.ac.ui.cs.advprog.jsonbackend.wallet.event.model.WalletOutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WalletOutboxEventRepository extends JpaRepository<WalletOutboxEvent, UUID> {
    List<WalletOutboxEvent> findTop50ByStatusOrderByCreatedAtAsc(WalletOutboxEventStatus status);

    List<WalletOutboxEvent> findTop50ByStatusOrderByOccurredAtAsc(WalletOutboxEventStatus status);
}
