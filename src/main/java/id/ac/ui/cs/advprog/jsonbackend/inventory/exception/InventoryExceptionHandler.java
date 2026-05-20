package id.ac.ui.cs.advprog.jsonbackend.inventory.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class InventoryExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(InventoryExceptionHandler.class);

    @ExceptionHandler(ForbiddenInventoryAccessException.class)
    public ResponseEntity<Map<String, String>> handleForbiddenInventoryAccessException(
            ForbiddenInventoryAccessException ex
    ) {
        log.warn("Inventory access forbidden: {}", ex.getMessage());
        return ResponseEntity
                .status(403)
                .body(Map.of("message", ex.getMessage()));
    }
}
