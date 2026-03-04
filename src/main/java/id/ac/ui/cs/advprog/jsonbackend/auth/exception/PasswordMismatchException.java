package id.ac.ui.cs.advprog.jsonbackend.auth.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class PasswordMismatchException extends ResponseStatusException {
    public PasswordMismatchException() {
        super(HttpStatus.BAD_REQUEST, "Password dan konfirmasi password tidak cocok");
    }
}