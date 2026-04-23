package id.ac.ui.cs.advprog.jsonbackend.inventory.event.handler;

import id.ac.ui.cs.advprog.jsonbackend.inventory.event.StockReservationRequestedEvent;
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
class StockReservationRequestedEventHandlerTest {

    @Mock
    private ProductService productService;

    @Mock
    private ProcessedEventRepository processedEventRepository;

    @InjectMocks
    private StockReservationRequestedEventHandler handler;

    @Test
    void handleShouldReserveStockAndMarkProcessedWhenEventIsNew() {
        UUID eventId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        StockReservationRequestedEvent event = new StockReservationRequestedEvent(eventId, productId, 2, "corr-1");

        when(processedEventRepository.existsByEventIdAndHandlerName(eventId, "StockReservationRequestedEventHandler"))
                .thenReturn(false);

        handler.handle(event);

        verify(productService, times(1)).reserveStock(productId, 2);

        ArgumentCaptor<ProcessedEvent> captor = ArgumentCaptor.forClass(ProcessedEvent.class);
        verify(processedEventRepository, times(1)).save(captor.capture());
        assertEquals(eventId, captor.getValue().getEventId());
        assertEquals("StockReservationRequestedEventHandler", captor.getValue().getHandlerName());
    }

    @Test
    void handleShouldSkipWhenEventAlreadyProcessed() {
        UUID eventId = UUID.randomUUID();
        UUID productId = UUID.randomUUID();
        StockReservationRequestedEvent event = new StockReservationRequestedEvent(eventId, productId, 1, "corr-2");

        handler.handle(event);

        verify(processedEventRepository, times(1))
                .existsByEventIdAndHandlerName(eventId, "StockReservationRequestedEventHandler");
        verify(productService, never()).reserveStock(productId, 1);
        verify(processedEventRepository, never()).save(org.mockito.ArgumentMatchers.any());
    }
}
