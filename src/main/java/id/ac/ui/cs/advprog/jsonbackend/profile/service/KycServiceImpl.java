package id.ac.ui.cs.advprog.jsonbackend.profile.service;

import id.ac.ui.cs.advprog.jsonbackend.auth.enums.AccountStatus;
import id.ac.ui.cs.advprog.jsonbackend.auth.model.User;
import id.ac.ui.cs.advprog.jsonbackend.auth.repository.UserRepository;
import id.ac.ui.cs.advprog.jsonbackend.profile.dto.KycRequest;
import id.ac.ui.cs.advprog.jsonbackend.profile.exception.UserProfileNotFoundException;
import id.ac.ui.cs.advprog.jsonbackend.profile.model.UserProfile;
import id.ac.ui.cs.advprog.jsonbackend.profile.repository.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
        user.setAccountStatus(AccountStatus.PENDING_VERIFICATION);
        userRepository.save(user);

        profile.setFullName(request.fullName());
        return userProfileRepository.save(profile);
    }
}