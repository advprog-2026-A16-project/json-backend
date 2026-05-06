package id.ac.ui.cs.advprog.jsonbackend.profile.service;

import id.ac.ui.cs.advprog.jsonbackend.auth.enums.AccountStatus;
import id.ac.ui.cs.advprog.jsonbackend.auth.enums.Role;
import id.ac.ui.cs.advprog.jsonbackend.auth.model.User;
import id.ac.ui.cs.advprog.jsonbackend.auth.repository.UserRepository;
import id.ac.ui.cs.advprog.jsonbackend.profile.dto.KycRequest;
import id.ac.ui.cs.advprog.jsonbackend.profile.exception.UserProfileNotFoundException;
import id.ac.ui.cs.advprog.jsonbackend.profile.model.UserProfile;
import id.ac.ui.cs.advprog.jsonbackend.profile.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KycServiceImpl implements KycService {
    private final UserRepository userRepository;
    private final UserProfileRepository userProfileRepository;

    @Override
    @Transactional
    public UserProfile submitKyc(UUID userId, KycRequest request) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new UserProfileNotFoundException("Profile tidak ditemukan"));

        User user = profile.getUser();
        if (user.getAccountStatus() == AccountStatus.PENDING_VERIFICATION) {
            throw new IllegalStateException("Pengajuan KYC sedang diproses");
        }

        user.setAccountStatus(AccountStatus.PENDING_VERIFICATION);
        userRepository.save(user);

        if (profile.getFullName() == null || profile.getFullName().isBlank()) {
            profile.setFullName(request.fullName());
        }

        profile.setKycFullName(request.fullName());
        profile.setIdentityNumber(request.identityNumber());
        profile.setSocialMediaLink(request.socialMediaLink());

        return userProfileRepository.save(profile);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserProfile> getPendingKycList() {
        return userProfileRepository.findAllByUserAccountStatus(AccountStatus.PENDING_VERIFICATION);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserProfile> getAllUsers() {
        return userProfileRepository.findAll();
    }

    @Override
    @Transactional
    public UserProfile approveKyc(UUID userId) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new UserProfileNotFoundException("Profile tidak ditemukan"));
        User user = profile.getUser();

        if (user.getAccountStatus() != AccountStatus.PENDING_VERIFICATION) {
            throw new IllegalStateException("Pengguna tidak dalam status PENDING_VERIFICATION");
        }

        user.setRole(Role.JASTIPER);
        user.setAccountStatus(AccountStatus.ACTIVE);
        userRepository.save(user);

        profile.setVerifiedJastiper(true);
        return userProfileRepository.save(profile);
    }

    @Override
    @Transactional
    public UserProfile rejectKyc(UUID userId) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new UserProfileNotFoundException("Profile tidak ditemukan"));
        User user = profile.getUser();

        if (user.getAccountStatus() != AccountStatus.PENDING_VERIFICATION) {
            throw new IllegalStateException("Pengguna tidak dalam status PENDING_VERIFICATION");
        }

        user.setAccountStatus(AccountStatus.ACTIVE);
        userRepository.save(user);

        profile.setKycFullName(null);
        profile.setIdentityNumber(null);
        profile.setSocialMediaLink(null);

        return userProfileRepository.save(profile);
    }

    @Override
    @Transactional
    public UserProfile updateUserStatus(UUID userId, AccountStatus status, Role role) {
        UserProfile profile = userProfileRepository.findByUserId(userId)
                .orElseThrow(() -> new UserProfileNotFoundException("Profile tidak ditemukan"));
        User user = profile.getUser();

        user.setAccountStatus(status);
        user.setRole(role);
        
        if (role == Role.TITIPERS) {
            profile.setVerifiedJastiper(false);
        }

        userRepository.save(user);
        return userProfileRepository.save(profile);
    }
}