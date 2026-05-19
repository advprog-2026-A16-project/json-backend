package id.ac.ui.cs.advprog.jsonbackend.wallet.payment;

import id.ac.ui.cs.advprog.jsonbackend.wallet.dto.MidtransNotificationRequest;

public interface PaymentNotificationService {
    void handleMidtransNotification(MidtransNotificationRequest notification);
}
