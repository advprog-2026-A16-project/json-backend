package id.ac.ui.cs.advprog.jsonbackend.profile.exception;

public class UserProfileNotFoundException extends RuntimeException {
    public UserProfileNotFoundException(String message) {
        super(message);
    }
}