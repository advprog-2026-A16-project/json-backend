package id.ac.ui.cs.advprog.jsonbackend.inventory.event.handler;

import id.ac.ui.cs.advprog.jsonbackend.inventory.event.StockReservationRequestedEvent;
import id.ac.ui.cs.advprog.jsonbackend.inventory.event.repository.ProcessedEventRepository;
import id.ac.ui.cs.advprog.jsonbackend.inventory.service.ProductService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class StockReservationRequestedEventHandler {

    private final ProductService productService;
    private final ProcessedEventRepository processedEventRepository;

    public StockReservationRequestedEventHandler(ProductService productService,
                                                 ProcessedEventRepository processedEventRepository) {
        this.productService = productService;
        this.processedEventRepository = processedEventRepository;
    }

    @Transactional
    public void handle(StockReservationRequestedEvent event) {
        // RED phase skeleton: implementation will be added in GREEN step.
    }
}
