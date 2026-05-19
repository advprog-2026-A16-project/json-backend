package id.ac.ui.cs.advprog.jsonbackend.auth.service;

import id.ac.ui.cs.advprog.jsonbackend.auth.dto.AuthResponse;
import id.ac.ui.cs.advprog.jsonbackend.auth.dto.LoginRequest;
import id.ac.ui.cs.advprog.jsonbackend.auth.dto.RegisterRequest;
import id.ac.ui.cs.advprog.jsonbackend.auth.enums.AccountStatus;
import id.ac.ui.cs.advprog.jsonbackend.auth.event.AuthEventPayloadFactory;
import id.ac.ui.cs.advprog.jsonbackend.auth.event.enums.AuthEventType;
import id.ac.ui.cs.advprog.jsonbackend.auth.event.model.AuthOutboxEvent;
import id.ac.ui.cs.advprog.jsonbackend.auth.event.repository.AuthOutboxEventRepository;
import id.ac.ui.cs.advprog.jsonbackend.auth.exception.AccountBannedException;
import id.ac.ui.cs.advprog.jsonbackend.auth.exception.EmailAlreadyRegisteredException;
import id.ac.ui.cs.advprog.jsonbackend.auth.exception.PasswordMismatchException;
import id.ac.ui.cs.advprog.jsonbackend.auth.exception.UserNotFoundException;
import id.ac.ui.cs.advprog.jsonbackend.auth.enums.Role;
import id.ac.ui.cs.advprog.jsonbackend.auth.model.User;
import id.ac.ui.cs.advprog.jsonbackend.auth.model.Profile;
import id.ac.ui.cs.advprog.jsonbackend.auth.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final ProfileService profileService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final AuthOutboxEventRepository outboxEventRepository;

    public AuthServiceImpl(UserRepository userRepository,
                           ProfileService profileService,
                           PasswordEncoder passwordEncoder,
                           JwtService jwtService,
                           AuthenticationManager authenticationManager,
                           AuthOutboxEventRepository outboxEventRepository) {
        this.userRepository = userRepository;
        this.profileService = profileService;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.outboxEventRepository = outboxEventRepository;
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        validateRegisterRequest(request);

        User user = new User(
                request.email(),
                passwordEncoder.encode(request.password()),
                Role.TITIPERS
        );
        userRepository.save(user);

        String generatedUsername = user.getEmail().split("@")[0];
        Profile profile = profileService.createProfileForUser(user, generatedUsername);

        UUID eventId = UUID.randomUUID();
        String correlationId = UUID.randomUUID().toString();

        String jsonPayload = AuthEventPayloadFactory.userRegisteredPayload(user, profile);

        AuthOutboxEvent outboxEvent = AuthOutboxEvent.builder()
                .eventId(eventId)
                .eventType(AuthEventType.USER_REGISTERED.name())
                .aggregateId(user.getId())
                .payload(jsonPayload)
                .correlationId(correlationId)
                .build();

        outboxEventRepository.save(outboxEvent);

        String jwtToken = jwtService.generateToken(user);
        return new AuthResponse(jwtToken, "Registration successful", user.getEmail(), user.getRole(), user.getId());
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (user.getAccountStatus() == AccountStatus.BANNED) {
            throw new AccountBannedException("Your account has been banned.");
        }

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );

        String jwtToken = jwtService.generateToken(user);
        return new AuthResponse(jwtToken, "Login successful", user.getEmail(), user.getRole(), user.getId());
    }

    private void validateRegisterRequest(RegisterRequest request) {
        if (!request.password().equals(request.confirmPassword())) {
            throw new PasswordMismatchException("The password and confirmation password do not match.");
        }
        if (userRepository.findByEmail(request.email()).isPresent()) {
            throw new EmailAlreadyRegisteredException(
                    "Email already registered"
            );
        }
    }
}
