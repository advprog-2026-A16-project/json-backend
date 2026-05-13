package id.ac.ui.cs.advprog.jsonbackend.inventory.event.handler;

import id.ac.ui.cs.advprog.jsonbackend.inventory.event.StockReleaseRequestedEvent;
import id.ac.ui.cs.advprog.jsonbackend.inventory.event.model.ProcessedEvent;
import id.ac.ui.cs.advprog.jsonbackend.inventory.event.repository.ProcessedEventRepository;
import id.ac.ui.cs.advprog.jsonbackend.inventory.service.ProductService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class StockReleaseRequestedEventHandlerTest {

    @Mock
    private ProductService productService;

    @Mock
    private ProcessedEventRepository processedEventRepository;

    @InjectMocks
    private StockReleaseRequestedEventHandler handler;

    @Test
    void handleShouldReleaseStockAndMarkProcessedWhenEventIsNew() {
        UUID eventId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        StockReleaseRequestedEvent event = new StockReleaseRequestedEvent(eventId, productId, 2, "corr-release-1");

        when(processedEventRepository.existsByEventIdAndHandlerName(eventId, "StockReleaseRequestedEventHandler"))
                .thenReturn(false);

        handler.handle(event);

        verify(productService, times(1)).releaseStock(productId, 2);

        ArgumentCaptor<ProcessedEvent> captor = ArgumentCaptor.forClass(ProcessedEvent.class);
        verify(processedEventRepository, times(1)).save(captor.capture());
        assertEquals(eventId, captor.getValue().getEventId());
        assertEquals("StockReleaseRequestedEventHandler", captor.getValue().getHandlerName());
    }

    @Test
    void handleShouldSkipWhenEventAlreadyProcessed() {
        UUID eventId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        StockReleaseRequestedEvent event = new StockReleaseRequestedEvent(eventId, productId, 1, "corr-release-2");

        when(processedEventRepository.existsByEventIdAndHandlerName(eventId, "StockReleaseRequestedEventHandler"))
                .thenReturn(true);

        handler.handle(event);

        verify(processedEventRepository, times(1))
                .existsByEventIdAndHandlerName(eventId, "StockReleaseRequestedEventHandler");
        verify(productService, never()).releaseStock(productId, 1);
        verify(processedEventRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }
}
