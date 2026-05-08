package id.ac.ui.cs.advprog.jsonbackend.auth.service;

import id.ac.ui.cs.advprog.jsonbackend.auth.enums.AccountStatus;
import id.ac.ui.cs.advprog.jsonbackend.auth.enums.Role;
import id.ac.ui.cs.advprog.jsonbackend.auth.model.User;
import id.ac.ui.cs.advprog.jsonbackend.auth.repository.UserRepository;
import id.ac.ui.cs.advprog.jsonbackend.auth.dto.KycRequest;
import id.ac.ui.cs.advprog.jsonbackend.auth.exception.ProfileNotFoundException;
import id.ac.ui.cs.advprog.jsonbackend.auth.model.Profile;
import id.ac.ui.cs.advprog.jsonbackend.auth.repository.ProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class KycServiceImpl implements KycService {
    private final UserRepository userRepository;
    private final ProfileRepository profileRepository;

    @Override
    @Transactional
    public Profile submitKyc(UUID userId, KycRequest request) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ProfileNotFoundException("Profile tidak ditemukan"));

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

        return profileRepository.save(profile);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Profile> getPendingKycList() {
        return profileRepository.findAllByUserAccountStatus(AccountStatus.PENDING_VERIFICATION);
    }

    @Override
    @Transactional(readOnly = true)
    public List<Profile> getAllUsers() {
        return profileRepository.findAll();
    }

    @Override
    @Transactional
    public Profile approveKyc(UUID userId) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ProfileNotFoundException("Profile tidak ditemukan"));
        User user = profile.getUser();

        if (user.getAccountStatus() != AccountStatus.PENDING_VERIFICATION) {
            throw new IllegalStateException("Pengguna tidak dalam status PENDING_VERIFICATION");
        }

        user.setRole(Role.JASTIPER);
        user.setAccountStatus(AccountStatus.ACTIVE);
        userRepository.save(user);

        profile.setVerifiedJastiper(true);
        return profileRepository.save(profile);
    }

    @Override
    @Transactional
    public Profile rejectKyc(UUID userId) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ProfileNotFoundException("Profile tidak ditemukan"));
        User user = profile.getUser();

        if (user.getAccountStatus() != AccountStatus.PENDING_VERIFICATION) {
            throw new IllegalStateException("Pengguna tidak dalam status PENDING_VERIFICATION");
        }

        user.setAccountStatus(AccountStatus.ACTIVE);
        userRepository.save(user);

        profile.setKycFullName(null);
        profile.setIdentityNumber(null);
        profile.setSocialMediaLink(null);

        return profileRepository.save(profile);
    }

    @Override
    @Transactional
    public Profile updateUserStatus(UUID userId, AccountStatus status, Role role) {
        Profile profile = profileRepository.findByUserId(userId)
                .orElseThrow(() -> new ProfileNotFoundException("Profile tidak ditemukan"));
        User user = profile.getUser();

        user.setAccountStatus(status);
        user.setRole(role);

        if (role == Role.TITIPERS) {
            profile.setVerifiedJastiper(false);
        }

        userRepository.save(user);
        return profileRepository.save(profile);
    }
}