package id.ac.ui.cs.advprog.jsonbackend.inventory.exception;

public class ForbiddenInventoryAccessException extends RuntimeException {
    public ForbiddenInventoryAccessException(String message) {
        super(message);
    }
}
