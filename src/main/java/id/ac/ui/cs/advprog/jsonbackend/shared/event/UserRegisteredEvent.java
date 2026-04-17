package id.ac.ui.cs.advprog.jsonbackend.shared.event;

import id.ac.ui.cs.advprog.jsonbackend.auth.model.User;

public record UserRegisteredEvent(User user) {}