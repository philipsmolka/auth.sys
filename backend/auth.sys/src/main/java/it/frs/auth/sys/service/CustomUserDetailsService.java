package it.frs.auth.sys.service;

import it.frs.auth.sys.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

/**
 * Custom implementation of Spring Security's {@link UserDetailsService}.
 * <p>
 * This service is responsible for loading user-specific data from the database.
 * Spring Security uses this service during the authentication process to retrieve user
 * details (like password and authorities) by their username. In this application,
 * the user's email address serves as the unique username.
 *
 * @see UserDetailsService
 * @see it.frs.auth.sys.model.User
 */
@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    /**
     * The repository for accessing user data from the database.
     */
    private final UserRepository userRepository;

    /**
     * Locates the user based on their email address.
     * This method is the core of the {@link UserDetailsService} interface and is called by
     * Spring Security's authentication provider.
     *
     * @param email the email address, which is used as the username for finding the user.
     * @return a {@link UserDetails} object (in this case, our {@code User} entity) that Spring Security can use for authentication and validation.
     * @throws UsernameNotFoundException if no user could be found with the given email address.
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // Note: The User entity must implement the UserDetails interface.
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("User with email " + email + " not found."));
    }
}