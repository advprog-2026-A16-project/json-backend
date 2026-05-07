package id.ac.ui.cs.advprog.jsonbackend.auth.exception;

public class UserProfileNotFoundException extends RuntimeException {
    public UserProfileNotFoundException(String message) {
        super(message);
    }
}