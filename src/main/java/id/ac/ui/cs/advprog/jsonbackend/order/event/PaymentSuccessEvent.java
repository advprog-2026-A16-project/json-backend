package id.ac.ui.cs.advprog.jsonbackend.order.event;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PaymentSuccessEvent {
    private UUID orderId;
}