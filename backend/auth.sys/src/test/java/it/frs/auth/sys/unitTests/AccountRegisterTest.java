package it.frs.auth.sys.unitTests;

import it.frs.auth.sys.dto.UserRequest;
import it.frs.auth.sys.dto.UserResponse;
import it.frs.auth.sys.exception.ResourceAlreadyExistsException;
import it.frs.auth.sys.model.User;
import it.frs.auth.sys.repository.UserRepository;
import it.frs.auth.sys.service.AccountRegisterService;
import it.frs.auth.sys.service.EmailService;
import it.frs.auth.sys.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class AccountRegisterTest {


    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private EmailService emailService;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AccountRegisterService accountRegisterService;


    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void registerUser_validRequest_savesUserAndSendsEmail(){

        String userEmail = "test@example.com";
        String userPassword = "password";
        String userName = "Example user";

        UserRequest.Register request = new UserRequest.Register(userEmail, userPassword, userName);

        when(userRepository.existsByEmailOrName(userEmail, userName)).thenReturn(false);

        when(passwordEncoder.encode(userPassword)).thenReturn("hashedPassword");

        User mockUser = new User();
        mockUser.setId(1L);
        mockUser.setEmail(userEmail);
        mockUser.setName(userName);
        mockUser.setPassword("hashedPassword");
        mockUser.setRoles(List.of("ROLE_USER"));
        mockUser.setActive(false);

        when(userRepository.save(any(User.class))).thenReturn(mockUser);
        when(jwtService.generateAccountConfirmationToken(userEmail)).thenReturn("confirmationToken");

        UserResponse.UserDetails response = accountRegisterService.registerUser(request);

        assertNotNull(response);
        assertEquals(1L, response.id());
        assertEquals(userEmail, response.email());
        assertEquals(userName, response.name());
        assertFalse(mockUser.isEnabled());
        assertTrue(mockUser.getRoles().contains("ROLE_USER"));

        verify(userRepository).save(any(User.class));
        verify(emailService).sendConfirmationEmail(userEmail, "confirmationToken");
        verify(passwordEncoder).encode(userPassword);
        verify(jwtService).generateAccountConfirmationToken(userEmail);

    }

    @Test
    void registerUser_emailOrNameAlreadyExists_throwsResourceAlreadyExistsException(){

        String userEmail = "test@example.com";
        String userPassword = "password";
        String userName = "Example user";

        UserRequest.Register request = new UserRequest.Register(userEmail, userPassword, userName);

        when(userRepository.existsByEmailOrName(userEmail, userName)).thenReturn(true);

        assertThrows(ResourceAlreadyExistsException.class,
                () -> accountRegisterService.registerUser(request));

        verify(userRepository).existsByEmailOrName(userEmail, userName);

        verifyNoMoreInteractions(userRepository, passwordEncoder, jwtService, emailService);

    }

    @Test
    void registerUser_passwordIsEncoded_beforeSavingUser(){

        String userEmail = "test@example.com";
        String userPassword = "password";
        String userName = "Example user";

        UserRequest.Register request = new UserRequest.Register(userEmail, userPassword, userName);

        when(userRepository.existsByEmailOrName(userEmail, userName)).thenReturn(false);
        when(passwordEncoder.encode(userPassword)).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        accountRegisterService.registerUser(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertNotEquals(userPassword, savedUser.getPassword());
        verify(passwordEncoder).encode(userPassword);

    }

    @Test
    void registerUser_newUser_hasDefaultRoleAndInactiveStatus(){

        String userEmail = "test@example.com";
        String userPassword = "password";
        String userName = "Example user";

        UserRequest.Register request = new UserRequest.Register(userEmail, userPassword, userName);

        when(userRepository.existsByEmailOrName(userEmail, userName)).thenReturn(false);
        when(passwordEncoder.encode(userPassword)).thenReturn("hashedPassword");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        accountRegisterService.registerUser(request);

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);

        verify(userRepository).save(userCaptor.capture());

        User savedUser = userCaptor.getValue();

        assertFalse(savedUser.isEnabled());
        assertTrue(savedUser.getRoles().contains("ROLE_USER"));

    }

}
