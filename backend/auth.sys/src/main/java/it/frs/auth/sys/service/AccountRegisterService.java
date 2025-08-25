package it.frs.auth.sys.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import it.frs.auth.sys.dto.UserRequest;
import it.frs.auth.sys.dto.UserResponse;
import it.frs.auth.sys.exception.InvalidInputException;
import it.frs.auth.sys.exception.ResourceAlreadyExistsException;
import it.frs.auth.sys.exception.ResourceNotFoundException;
import it.frs.auth.sys.model.User;
import it.frs.auth.sys.repository.UserRepository;

import java.util.List;


@SuppressWarnings("unused")
@Service
@RequiredArgsConstructor
@Slf4j
public class AccountRegisterService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final JwtService jwtService;

    private static final String ROLE_USER = "ROLE_USER";

    private UserResponse.UserDetails toUserDetails(User user) {
        return new UserResponse.UserDetails(user.getId(), user.getEmail(), user.getName(), user.getRoles(), user.isEnabled());
    }

    /**
     * Register a new user based on the request data.
     */
    @Transactional
    public UserResponse.UserDetails registerUser(UserRequest.Register request) {

        if (userRepository.existsByEmailOrName(request.email(), request.name())) {
            throw new ResourceAlreadyExistsException("User with that email or name already exists.");
        }

        User newUser = new User();
        newUser.setEmail(request.email());
        newUser.setName(request.name());
        newUser.setPassword(passwordEncoder.encode(request.password()));
        newUser.setRoles(List.of("ROLE_USER"));
        newUser.setActive(false);

        emailService.sendConfirmationEmail(request.email(), jwtService.generateAccountConfirmationToken(request.email()));

        User savedUser = userRepository.save(newUser);
        log.info("Attempt to register a new user: {}", savedUser.getEmail());

        return toUserDetails(savedUser);
    }

    @Transactional
    public void activateUser(String token) {

        String email = jwtService.extractEmail(token);

        User user = findUserByEmailOrThrow(email);

        if (user.isEnabled()) {
            throw new InvalidInputException("This account has already been activated.");
        }

        user.setActive(true);

        userRepository.save(user);

        log.info("User account {} has been successfully registered.", user.getEmail());
    }

    private User findUserByEmailOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User with email " + email + " not found."));
    }
}
