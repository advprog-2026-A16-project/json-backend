package id.ac.ui.cs.advprog.jsonbackend.auth.exception;

public class PasswordMismatchException extends RuntimeException {
    public PasswordMismatchException(String message) {
        super(message);
    }
}