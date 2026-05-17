package id.ac.ui.cs.advprog.jsonbackend.auth.event.service;

import id.ac.ui.cs.advprog.jsonbackend.auth.event.model.AuthOutboxEvent;

public interface AuthEventPublisher {
    void publish(AuthOutboxEvent event);
}