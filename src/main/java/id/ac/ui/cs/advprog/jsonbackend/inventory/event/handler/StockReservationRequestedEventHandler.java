package id.ac.ui.cs.advprog.jsonbackend.inventory.event.handler;

import id.ac.ui.cs.advprog.jsonbackend.common.monitoring.ApplicationMetrics;
import id.ac.ui.cs.advprog.jsonbackend.inventory.event.StockReservationRequestedEvent;
import id.ac.ui.cs.advprog.jsonbackend.inventory.event.model.ProcessedEvent;
import id.ac.ui.cs.advprog.jsonbackend.inventory.event.repository.ProcessedEventRepository;
import id.ac.ui.cs.advprog.jsonbackend.inventory.service.ProductService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;

@Component
public class StockReservationRequestedEventHandler {

    private static final Logger log = LoggerFactory.getLogger(StockReservationRequestedEventHandler.class);

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
            log.info("Inventory event: RESERVE_STOCK_SUCCESS eventId={} productId={} quantity={}",
                    event.eventId(), event.productId(), event.quantity());
            applicationMetrics.recordReserveStockSuccess(elapsed(startNanos));
        } catch (RuntimeException exception) {
            log.warn("Inventory event: RESERVE_STOCK_FAILURE eventId={} productId={} quantity={} reason={}",
                    event.eventId(), event.productId(), event.quantity(), exception.getClass().getSimpleName());
            applicationMetrics.recordReserveStockFailure(elapsed(startNanos));
            throw exception;
        }
    }

    private Duration elapsed(long startNanos) {
        return Duration.ofNanos(System.nanoTime() - startNanos);
    }
}
