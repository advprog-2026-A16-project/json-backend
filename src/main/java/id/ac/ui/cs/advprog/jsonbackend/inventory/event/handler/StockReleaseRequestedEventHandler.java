package id.ac.ui.cs.advprog.jsonbackend.inventory.event.handler;

import id.ac.ui.cs.advprog.jsonbackend.inventory.event.StockReleaseRequestedEvent;
import id.ac.ui.cs.advprog.jsonbackend.inventory.event.model.ProcessedEvent;
import id.ac.ui.cs.advprog.jsonbackend.inventory.event.repository.ProcessedEventRepository;
import id.ac.ui.cs.advprog.jsonbackend.inventory.service.ProductService;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Component
public class StockReleaseRequestedEventHandler {

    private final ProductService productService;
    private final ProcessedEventRepository processedEventRepository;

    public StockReleaseRequestedEventHandler(ProductService productService,
                                             ProcessedEventRepository processedEventRepository) {
        this.productService = productService;
        this.processedEventRepository = processedEventRepository;
    }

    @EventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(StockReleaseRequestedEvent event) {
        String handlerName = "StockReleaseRequestedEventHandler";
        boolean alreadyProcessed =
                processedEventRepository.existsByEventIdAndHandlerName(event.eventId(), handlerName);

        if (alreadyProcessed) {
            return;
        }

        productService.releaseStock(event.productId(), event.quantity());
        processedEventRepository.save(ProcessedEvent.builder()
                .eventId(event.eventId())
                .handlerName(handlerName)
                .build());
    }
}
