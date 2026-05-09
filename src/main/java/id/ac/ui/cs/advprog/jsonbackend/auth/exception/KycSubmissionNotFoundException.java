package id.ac.ui.cs.advprog.jsonbackend.auth.exception;

public class KycSubmissionNotFoundException extends RuntimeException {
    public KycSubmissionNotFoundException(String message) {
        super(message);
    }
}