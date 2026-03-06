package id.ac.ui.cs.advprog.jsonbackend.wallet.exception;

public class WalletNotFoundException extends RuntimeException {

    public WalletNotFoundException() {
        super("Wallet tidak ditemukan");
    }

    public WalletNotFoundException(String message) {
        super(message);
    }
}
