package id.ac.ui.cs.advprog.jsonbackend.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class EmailAlreadyRegisteredException extends ResponseStatusException {
    public EmailAlreadyRegisteredException() {
        super(HttpStatus.CONFLICT, "Email already registered");
    }
}
