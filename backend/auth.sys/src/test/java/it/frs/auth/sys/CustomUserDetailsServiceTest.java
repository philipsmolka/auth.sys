package it.frs.auth.sys;

import it.frs.auth.sys.model.User;
import it.frs.auth.sys.repository.UserRepository;
import it.frs.auth.sys.service.AuthService;
import it.frs.auth.sys.service.CustomUserDetailsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

public class CustomUserDetailsServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private CustomUserDetailsService customUserDetailsService;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void loadUserByUsername_existingEmail_returnsUserDetails() {
        String email = "test@example.com";
        User user = new User();
        user.setEmail(email);

        when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

        UserDetails result = customUserDetailsService.loadUserByUsername(email);

        assertEquals(email, result.getUsername());
    }

    @Test
    void loadUserByUsername_nonExistingEmail_throwsException() {
        String email = "notfound@example.com";

        when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

        assertThrows(UsernameNotFoundException.class, () ->
                customUserDetailsService.loadUserByUsername(email)
        );
    }

}
