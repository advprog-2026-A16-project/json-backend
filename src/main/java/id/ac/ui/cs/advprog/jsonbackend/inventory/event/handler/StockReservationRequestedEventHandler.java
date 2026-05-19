package id.ac.ui.cs.advprog.jsonbackend.inventory.event.handler;

import id.ac.ui.cs.advprog.jsonbackend.common.monitoring.ApplicationMetrics;
import id.ac.ui.cs.advprog.jsonbackend.inventory.event.StockReservationRequestedEvent;
import id.ac.ui.cs.advprog.jsonbackend.inventory.event.model.ProcessedEvent;
import id.ac.ui.cs.advprog.jsonbackend.inventory.event.repository.ProcessedEventRepository;
import id.ac.ui.cs.advprog.jsonbackend.inventory.service.ProductService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Component
public class StockReservationRequestedEventHandler {

    private final ProductService productService;
    private final ProcessedEventRepository processedEventRepository;
    private final ApplicationMetrics applicationMetrics;

    public StockReservationRequestedEventHandler(ProductService productService,
                                                 ProcessedEventRepository processedEventRepository,
                                                 ApplicationMetrics applicationMetrics) {
        this.productService = productService;
        this.processedEventRepository = processedEventRepository;
        this.applicationMetrics = applicationMetrics;
    }

    @Transactional
    public void handle(StockReservationRequestedEvent event) {
        long startNanos = System.nanoTime();
        String handlerName = "StockReservationRequestedEventHandler";
        boolean alreadyProcessed =
                processedEventRepository.existsByEventIdAndHandlerName(event.eventId(), handlerName);

        if (alreadyProcessed) {
            return;
        }

        try {
            productService.reserveStock(event.productId(), event.quantity());
            processedEventRepository.save(ProcessedEvent.builder()
                    .eventId(event.eventId())
                    .handlerName(handlerName)
                    .build());
            applicationMetrics.recordReserveStockSuccess(elapsed(startNanos));
        } catch (RuntimeException exception) {
            applicationMetrics.recordReserveStockFailure(elapsed(startNanos));
            throw exception;
        }
    }

    private Duration elapsed(long startNanos) {
        return Duration.ofNanos(System.nanoTime() - startNanos);
    }
}
