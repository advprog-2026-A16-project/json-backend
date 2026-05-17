package id.ac.ui.cs.advprog.jsonbackend.inventory.event;

public class NonRetryableInventoryEventException extends RuntimeException {

    public NonRetryableInventoryEventException(String message) {
        super(message);
    }

    public NonRetryableInventoryEventException(String message, Throwable cause) {
        super(message, cause);
    }
}
