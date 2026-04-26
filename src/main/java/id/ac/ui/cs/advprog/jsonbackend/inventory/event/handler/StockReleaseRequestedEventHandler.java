package id.ac.ui.cs.advprog.jsonbackend.inventory.event.handler;

import id.ac.ui.cs.advprog.jsonbackend.inventory.event.StockReleaseRequestedEvent;
import id.ac.ui.cs.advprog.jsonbackend.inventory.event.repository.ProcessedEventRepository;
import id.ac.ui.cs.advprog.jsonbackend.inventory.service.ProductService;
import org.springframework.stereotype.Component;
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

    @Transactional
    public void handle(StockReleaseRequestedEvent event) {
        // RED skeleton: logic implemented in GREEN step.
    }
}
