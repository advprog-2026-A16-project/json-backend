package id.ac.ui.cs.advprog.jsonbackend.wallet.event.service;

import id.ac.ui.cs.advprog.jsonbackend.wallet.event.model.WalletOutboxEvent;

public interface WalletEventPublisher {
    void publish(WalletOutboxEvent event);
}
