package id.ac.ui.cs.advprog.jsonbackend.auth.exception;

public class ProfileNotFoundException extends RuntimeException {
    public ProfileNotFoundException(String message) {
        super(message);
    }
}