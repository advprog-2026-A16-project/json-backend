package id.ac.ui.cs.advprog.jsonbackend.auth.event.handler;

import id.ac.ui.cs.advprog.jsonbackend.auth.model.User;
import id.ac.ui.cs.advprog.jsonbackend.auth.repository.UserRepository;
import id.ac.ui.cs.advprog.jsonbackend.auth.event.model.UserProfileProcessedEvent;
import id.ac.ui.cs.advprog.jsonbackend.auth.event.repository.UserProfileProcessedEventRepository;
import id.ac.ui.cs.advprog.jsonbackend.auth.model.UserProfile;
import id.ac.ui.cs.advprog.jsonbackend.auth.repository.UserProfileRepository;
import id.ac.ui.cs.advprog.jsonbackend.auth.event.UserRegisteredEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class UserRegisteredEventHandler {
    private final UserProfileRepository userProfileRepository;
    private final UserRepository userRepository;
    private final UserProfileProcessedEventRepository processedEventRepository;

    public UserRegisteredEventHandler(UserProfileRepository userProfileRepository,
                                      UserRepository userRepository,
                                      UserProfileProcessedEventRepository processedEventRepository) {
        this.userProfileRepository = userProfileRepository;
        this.userRepository = userRepository;
        this.processedEventRepository = processedEventRepository;
    }

    @EventListener
    @Transactional
    public void handle(UserRegisteredEvent event) {
        String handlerName = "UserRegisteredEventHandler";

        if (processedEventRepository.existsByEventIdAndHandlerName(event.eventId(), handlerName)) {
            return;
        }

        User userRef = userRepository.findById(event.userId())
                .orElseThrow(() -> new IllegalStateException("User not found for profile creation"));

        String email = event.email();
        String generatedUsername = email.split("@")[0];

        UserProfile newProfile = UserProfile.builder()
                .user(userRef)
                .username(generatedUsername)
                .build();
        userProfileRepository.save(newProfile);

        processedEventRepository.save(UserProfileProcessedEvent.builder()
                .eventId(event.eventId())
                .handlerName(handlerName)
                .build());
    }
}