package id.ac.ui.cs.advprog.jsonbackend.wallet.event;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class PaymentSuccessEvent {
    private String orderId;
}