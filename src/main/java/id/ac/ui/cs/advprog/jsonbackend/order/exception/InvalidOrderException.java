package id.ac.ui.cs.advprog.jsonbackend.order.exception;

public class InvalidOrderException extends RuntimeException {
    public InvalidOrderException(String message) {
        super(message);
    }
}