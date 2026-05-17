package id.ac.ui.cs.advprog.jsonbackend.auth.service;

import id.ac.ui.cs.advprog.jsonbackend.auth.enums.AccountStatus;
import id.ac.ui.cs.advprog.jsonbackend.auth.enums.Role;
import id.ac.ui.cs.advprog.jsonbackend.auth.model.Profile;

import java.util.List;
import java.util.UUID;

public interface AdminUserService {
    List<Profile> getAllUsers();
    Profile updateUserStatus(UUID userId, AccountStatus status, Role role);
}