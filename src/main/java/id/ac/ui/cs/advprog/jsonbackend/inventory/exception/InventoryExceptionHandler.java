package id.ac.ui.cs.advprog.jsonbackend.inventory.exception;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice(basePackages = "id.ac.ui.cs.advprog.jsonbackend.inventory")
public class InventoryExceptionHandler {

    @ExceptionHandler(ForbiddenInventoryAccessException.class)
    public ResponseEntity<Map<String, String>> handleForbiddenInventoryAccessException(
            ForbiddenInventoryAccessException ex
    ) {
        return ResponseEntity
                .status(403)
                .body(Map.of("message", ex.getMessage()));
    }
}
