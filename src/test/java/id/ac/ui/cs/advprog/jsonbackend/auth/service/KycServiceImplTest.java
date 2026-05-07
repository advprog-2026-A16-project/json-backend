package id.ac.ui.cs.advprog.jsonbackend.auth.service;

import id.ac.ui.cs.advprog.jsonbackend.auth.enums.AccountStatus;
import id.ac.ui.cs.advprog.jsonbackend.auth.enums.Role;
import id.ac.ui.cs.advprog.jsonbackend.auth.model.User;
import id.ac.ui.cs.advprog.jsonbackend.auth.repository.UserRepository;
import id.ac.ui.cs.advprog.jsonbackend.auth.dto.KycRequest;
import id.ac.ui.cs.advprog.jsonbackend.auth.model.UserProfile;
import id.ac.ui.cs.advprog.jsonbackend.auth.repository.UserProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class KycServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private UserProfileRepository userProfileRepository;

    @InjectMocks
    private KycServiceImpl kycService;

    @Test
    void submitKyc_ShouldUpdateAccountStatusToPendingVerification_WhenDataIsValid() {
        UUID userId = UUID.randomUUID();
        User user = User.builder().id(userId).email("user@email.com").role(Role.TITIPERS).accountStatus(AccountStatus.ACTIVE).build();
        UserProfile existingProfile = UserProfile.builder().user(user).username("user").build();

        when(userProfileRepository.findByUserId(userId)).thenReturn(Optional.of(existingProfile));
        when(userProfileRepository.save(any(UserProfile.class))).thenAnswer(i -> i.getArguments()[0]);
        when(userRepository.save(any(User.class))).thenAnswer(i -> i.getArguments()[0]);

        KycRequest request = new KycRequest("Leon S. Kennedy", "3171234567890123", "https://instagram.com/leon");
        UserProfile updatedProfile = kycService.submitKyc(userId, request);

        assertEquals(AccountStatus.PENDING_VERIFICATION, updatedProfile.getUser().getAccountStatus());
        assertEquals("Leon S. Kennedy", updatedProfile.getFullName());
        verify(userRepository, times(1)).save(any(User.class));
    }
}
