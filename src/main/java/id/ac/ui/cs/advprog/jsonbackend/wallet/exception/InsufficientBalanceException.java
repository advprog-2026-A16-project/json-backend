package id.ac.ui.cs.advprog.jsonbackend.wallet.exception;

public class InsufficientBalanceException extends RuntimeException {

    public InsufficientBalanceException() {
        super("Saldo tidak mencukupi");
    }

    public InsufficientBalanceException(String message) {
        super(message);
    }
}