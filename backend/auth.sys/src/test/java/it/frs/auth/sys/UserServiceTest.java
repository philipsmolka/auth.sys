package it.frs.auth.sys;

import it.frs.auth.sys.dto.UserRequest;
import it.frs.auth.sys.dto.UserResponse;
import it.frs.auth.sys.exception.InvalidInputException;
import it.frs.auth.sys.exception.ResourceAlreadyExistsException;
import it.frs.auth.sys.exception.ResourceNotFoundException;
import it.frs.auth.sys.model.User;
import it.frs.auth.sys.repository.UserRepository;
import it.frs.auth.sys.service.UserService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void addUser_success() {

        UserRequest.Add request = new UserRequest.Add(
                "test@example.com",
                "strongPassword",
                "Test Example",
                "ROLE_USER"

        );

        when(userRepository.existsByEmail(request.email())).thenReturn(false);
        when(passwordEncoder.encode(request.password())).thenReturn("encodedPassword");

        User savedUser = new User();
        savedUser.setEmail(request.email());
        savedUser.setName(request.name());
        savedUser.setPassword("encodedPassword");
        savedUser.setRoles(List.of(request.role()));
        savedUser.setActive(true);

        when(userRepository.save(any(User.class))).thenReturn(savedUser);

        var response = userService.addUser(request);

        assertEquals("test@example.com", response.email());
        assertEquals("Test Example", response.name());

        verify(userRepository).save(any(User.class));

    }

    @Test
    void addUser_emailExists_throwsException() {
        UserRequest.Add request = new UserRequest.Add(
                "existing@example.com",
                "strongPassword",
                "Jan Kowalski",
                "USER"
        );

        when(userRepository.existsByEmail(request.email())).thenReturn(true);

        assertThrows(ResourceAlreadyExistsException.class, () -> userService.addUser(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void addUser_invalidRole_throwsException() {
        UserRequest.Add request = new UserRequest.Add(
                "test@example.com",
                "strongPassword",
                "Jan Kowalski",
                "INVALID_ROLE"
        );

        when(userRepository.existsByEmail(request.email())).thenReturn(false);

        assertThrows(InvalidInputException.class, () -> userService.addUser(request));
        verify(userRepository, never()).save(any());
    }

    @Test
    void deleteUserById_success(){

        Long userId = 1L;

        User mockUser = new User();
        mockUser.setId(userId);


        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        UserResponse.SuccessMessage response = userService.deleteUserById(userId);

        verify(userRepository).delete(mockUser);

        assertEquals("User deleted successfully.", response.message());

    }

    @Test
    void deleteUserById_notFound_throwsException() {

        Long userId = 2L;

        when(userRepository.findById(userId)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUserById(userId));

        verify(userRepository, never()).delete(any());

    }

    @Test
    void deleteUserByName_success(){

        Long userId = 1L;
        String userName = "Example Test";

        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setName(userName);

        when(userRepository.findByName(userName)).thenReturn(Optional.of(mockUser));

        UserResponse.SuccessMessage response = userService.deleteUserByName(userName);

        verify(userRepository).delete(mockUser);

        assertEquals("User deleted successfully.", response.message());

    }

    @Test
    void deleteUserByName_notFound_throwsException() {

        String userName = "Example Test";

        when(userRepository.findByName(userName)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> userService.deleteUserByName(userName));

        verify(userRepository, never()).delete(any());

    }

    @Test
    void deleteUserByEmail_success(){

        Long userId = 1L;
        String userEmail = "test@example.com";

        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setEmail(userEmail);

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(mockUser));

        UserResponse.SuccessMessage response = userService.deleteUserByEmail(userEmail);

        verify(userRepository).delete(mockUser);

        assertEquals("User deleted successfully.", response.message());

    }

    @Test
    void disableUserById_success(){

        Long userId = 1L;

        User mockUser = new User();
        mockUser.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        UserResponse.UserDetails response = userService.disableUserById(userId);

        verify(userRepository).delete(mockUser);

        assertEquals("User disabled successfully.", response.active());

    }

}
