package id.ac.ui.cs.advprog.jsonbackend.auth.exception;

import id.ac.ui.cs.advprog.jsonbackend.inventory.exception.InsufficientStockException;
import id.ac.ui.cs.advprog.jsonbackend.inventory.exception.InvalidProductException;
import id.ac.ui.cs.advprog.jsonbackend.inventory.exception.ProductNotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void handlesInvalidProductException() {
        ResponseEntity<Map<String, String>> response =
                handler.handleInvalidProductException(new InvalidProductException("bad product"));
        assertEquals(HttpStatus.BAD_REQUEST, response.getStatusCode());
        assertEquals("bad product", response.getBody().get("message"));
    }

    @Test
    void handlesInsufficientStockException() {
        ResponseEntity<Map<String, String>> response =
                handler.handleInsufficientStockException(new InsufficientStockException("stock low"));
        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals("stock low", response.getBody().get("message"));
    }

    @Test
    void handlesProductNotFoundException() {
        ResponseEntity<Map<String, String>> response =
                handler.handleProductNotFoundException(new ProductNotFoundException("missing"));
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals("missing", response.getBody().get("message"));
    }

    @Test
    void handlesResponseStatusException() {
        ResponseEntity<Map<String, String>> response =
                handler.handleResponseStatusException(new ResponseStatusException(HttpStatus.FORBIDDEN, "no access"));
        assertEquals(HttpStatus.FORBIDDEN, response.getStatusCode());
        assertEquals("no access", response.getBody().get("message"));
    }

    @Test
    void handlesAuthenticationException() {
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                handler.handleAuthenticationException(new BadCredentialsException("bad creds"));
        assertEquals(HttpStatus.UNAUTHORIZED, response.getStatusCode());
        assertEquals("Invalid email or password", response.getBody().message());
    }

    @Test
    void handlesGenericException() {
        ResponseEntity<GlobalExceptionHandler.ErrorResponse> response =
                handler.handleGenericException(new RuntimeException("boom"));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        assertEquals("An internal server error occurred", response.getBody().message());
    }

    @Test
    void handlesAuthDomainExceptions() {
        assertEquals(HttpStatus.FORBIDDEN,
                handler.handleAccountBannedException(new AccountBannedException("banned")).getStatusCode());
        assertEquals(HttpStatus.CONFLICT,
                handler.handleEmailAlreadyRegisteredException(new EmailAlreadyRegisteredException("dup email")).getStatusCode());
        assertEquals(HttpStatus.CONFLICT,
                handler.handleUsernameAlreadyExistsException(new UsernameAlreadyExistsException("dup username")).getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST,
                handler.handlePasswordMismatchException(new PasswordMismatchException("mismatch")).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND,
                handler.handleProfileNotFoundException(new ProfileNotFoundException("no profile")).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND,
                handler.handleUserNotFoundException(new UserNotFoundException("no user")).getStatusCode());
        assertEquals(HttpStatus.NOT_FOUND,
                handler.handleKycSubmissionNotFoundException(new KycSubmissionNotFoundException("no kyc")).getStatusCode());
    }
}
