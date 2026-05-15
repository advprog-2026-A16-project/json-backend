package id.ac.ui.cs.advprog.jsonbackend.order.event.service;

import id.ac.ui.cs.advprog.jsonbackend.order.event.OutboxEventStatus;
import id.ac.ui.cs.advprog.jsonbackend.order.event.model.OrderOutboxEvent;
import id.ac.ui.cs.advprog.jsonbackend.order.event.repository.OrderOutboxEventRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service("orderOutboxEventDispatcher")
public class OutboxEventDispatcher {

    private final OrderOutboxEventRepository outboxEventRepository;
    private final OrderEventPublisher orderEventPublisher;

    public OutboxEventDispatcher(OrderOutboxEventRepository outboxEventRepository,
                                 OrderEventPublisher orderEventPublisher) {
        this.outboxEventRepository = outboxEventRepository;
        this.orderEventPublisher = orderEventPublisher;
    }

    @Transactional
    public void dispatchPendingEvents() {
        List<OrderOutboxEvent> pendingEvents = outboxEventRepository
                .findByStatusOrderByCreatedAtAsc(OutboxEventStatus.PENDING);

        for (OrderOutboxEvent event : pendingEvents) {
            try {
                // Mencoba mempublikasikan pesan
                orderEventPublisher.publish(event);

                // Jika berhasil, perbarui status menjadi PROCESSED
                event.setStatus(OutboxEventStatus.PROCESSED);
                event.setSentAt(LocalDateTime.now());
                event.setFailureReason(null);
            } catch (Exception e) {
                // Jika gagal, catat alasan kegagalan dan ubah status menjadi FAILED
                event.setStatus(OutboxEventStatus.FAILED);
                event.setRetryCount(event.getRetryCount() + 1);
                event.setFailureReason(e.getMessage());
            }

            // Simpan perubahan status kembali ke database
            outboxEventRepository.save(event);
        }
    }
}
