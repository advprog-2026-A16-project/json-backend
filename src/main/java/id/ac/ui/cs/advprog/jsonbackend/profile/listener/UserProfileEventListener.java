package id.ac.ui.cs.advprog.jsonbackend.profile.listener;

import id.ac.ui.cs.advprog.jsonbackend.profile.model.UserProfile;
import id.ac.ui.cs.advprog.jsonbackend.profile.repository.UserProfileRepository;
import id.ac.ui.cs.advprog.jsonbackend.shared.event.UserRegisteredEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserProfileEventListener {

    private final UserProfileRepository userProfileRepository;

    @EventListener
    public void handleUserRegisteredEvent(UserRegisteredEvent event) {
        String email = event.user().getEmail();
        String generatedUsername = email.split("@")[0];

        UserProfile newProfile = UserProfile.builder()
                .user(event.user())
                .username(generatedUsername)
                .build();

        userProfileRepository.save(newProfile);
    }
}