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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
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

        verify(userRepository).save(mockUser);

        assertFalse(mockUser.isEnabled());

        assertEquals(false, response.active());

    }

    @Test
    void disableUserByName_success(){

        Long userId = 1L;
        String userName = "Example Test";

        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setName(userName);

        when(userRepository.findByName(userName)).thenReturn(Optional.of(mockUser));

        UserResponse.UserDetails response = userService.disableUserByName(userName);

        verify(userRepository).save(mockUser);

        assertFalse(mockUser.isEnabled());

        assertEquals(false, response.active());

    }

    @Test
    void disableUserByEmail_success(){

        Long userId = 1L;
        String userEmail = "test@example.com";

        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setEmail(userEmail);

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(mockUser));

        UserResponse.UserDetails response = userService.disableUserByEmail(userEmail);

        verify(userRepository).save(mockUser);

        assertFalse(mockUser.isEnabled());

        assertEquals(false, response.active());

    }

    @Test
    void enableUserById_success(){

        Long userId = 1L;

        User mockUser = new User();
        mockUser.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        UserResponse.UserDetails response = userService.enableUserById(userId);

        verify(userRepository).save(mockUser);

        assertTrue(mockUser.isEnabled());

        assertEquals(true, response.active());

    }

    @Test
    void enableUserByName_success(){

        Long userId = 1L;
        String userName = "Example Test";

        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setName(userName);

        when(userRepository.findByName(userName)).thenReturn(Optional.of(mockUser));

        UserResponse.UserDetails response = userService.enableUserByName(userName);

        verify(userRepository).save(mockUser);

        assertTrue(mockUser.isEnabled());

        assertEquals(true, response.active());

    }

    @Test
    void enableUserByEmail_success(){

        Long userId = 1L;
        String userEmail = "test@example.com";

        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setEmail(userEmail);

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(mockUser));

        UserResponse.UserDetails response = userService.enableUserByEmail(userEmail);

        verify(userRepository).save(mockUser);

        assertTrue(mockUser.isEnabled());

        assertEquals(true, response.active());

    }

    @Test
    void changePasswordById_success(){

        Long userId = 1L;

        UserRequest.ChangePassword request = new UserRequest.ChangePassword(
                "password"
        );

        User mockUser = new User();
        mockUser.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.encode(request.newPassword())).thenReturn("encodedPassword");


        UserResponse.SuccessMessage response = userService.changeUserPasswordById(userId, request);

        verify(userRepository).save(mockUser);
        assertEquals("encodedPassword", mockUser.getPassword());
        assertEquals("Password updated successfully.", response.message());

    }

    @Test
    void changePasswordByName_success(){

        Long userId = 1L;
        String userName = "Example Test";

        UserRequest.ChangePasswordByName request = new UserRequest.ChangePasswordByName(
                userName,
                "password"
        );

        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setName(userName);

        when(userRepository.findByName(userName)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.encode(request.newPassword())).thenReturn("encodedPassword");


        UserResponse.SuccessMessage response = userService.changeUserPasswordByName(request);

        verify(userRepository).save(mockUser);
        assertEquals("encodedPassword", mockUser.getPassword());
        assertEquals("Password updated successfully.", response.message());

    }

    @Test
    void changePasswordByEmail_success(){

        Long userId = 1L;
        String userEmail = "test@example.com";

        UserRequest.ChangePasswordByEmail request = new UserRequest.ChangePasswordByEmail(
                userEmail,
                "password"
        );

        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setEmail(userEmail);

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(mockUser));
        when(passwordEncoder.encode(request.newPassword())).thenReturn("encodedPassword");
        when(userRepository.save(mockUser)).thenReturn(mockUser);

        UserResponse.SuccessMessage response = userService.changeUserPasswordByEmail(request);

        verify(userRepository).save(mockUser);
        assertEquals("encodedPassword", mockUser.getPassword());
        assertEquals("Password updated successfully.", response.message());

    }

    @Test
    void changeUserRoleById(){

        Long userId = 1L;
        String userRole = "ROLE_USER";

        UserRequest.ChangeRole request = new UserRequest.ChangeRole(
                userRole
        );

        User mockUser = new User();
        mockUser.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(userRepository.save(mockUser)).thenReturn(mockUser);

        UserResponse.UserDetails response = userService.changeUserRoleById(userId, request);

        verify(userRepository).save(mockUser);

        assertTrue(response.roles().contains(userRole));

    }

    @Test
    void changeUserRoleByName(){

        Long userId = 1L;
        String userName = "Example Test";
        String userRole = "ROLE_USER";

        UserRequest.ChangeRoleByName request = new UserRequest.ChangeRoleByName(
                userName,
                userRole
        );

        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setName(userName);

        when(userRepository.findByName(userName)).thenReturn(Optional.of(mockUser));
        when(userRepository.save(mockUser)).thenReturn(mockUser);

        UserResponse.UserDetails response = userService.changeUserRoleByName(request);

        verify(userRepository).save(mockUser);

        assertTrue(response.roles().contains(userRole));

    }

    @Test
    void changeUserRoleByEmail(){

        Long userId = 1L;
        String userEmail = "example@test.com";
        String userRole = "ROLE_USER";

        UserRequest.ChangeRoleByEmail request = new UserRequest.ChangeRoleByEmail(
                userEmail,
                userRole
        );

        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setEmail(userEmail);

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(mockUser));
        when(userRepository.save(mockUser)).thenReturn(mockUser);

        UserResponse.UserDetails response = userService.changeUserRoleByEmail(request);

        verify(userRepository).save(mockUser);

        assertTrue(response.roles().contains(userRole));

    }

    @Test
    void updateUserDetailsById(){

        Long userId = 1L;
        String userName = "Example Test";
        String userEmail = "test@example.com";
        String userRole = "ROLE_USER";

        UserRequest.Update request = new UserRequest.Update(
                userEmail,
                userName,
                userRole
        );

        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setName("Test");
        mockUser.setEmail("test@test.com");
        mockUser.setRoles(List.of("USER_ROLE"));

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));
        when(userRepository.save(mockUser)).thenReturn(mockUser);

        UserResponse.UserDetails response = userService.updateUserDetailsById(userId, request);

        verify(userRepository).save(mockUser);

        assertTrue(response.name().contains(userName));
        assertTrue(response.email().contains(userEmail));
        assertTrue(response.roles().contains(userRole));

    }

    @Test
    void updateUserDetailsByName(){

        Long userId = 1L;
        String userNameToFind = "Test";
        String userName = "Example Test";
        String userEmail = "test@example.com";
        String userRole = "ROLE_USER";

        UserRequest.UpdateByName request = new UserRequest.UpdateByName(
                userNameToFind,
                userEmail,
                userName,
                userRole
        );

        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setName(userNameToFind);
        mockUser.setEmail("test@test.com");
        mockUser.setRoles(List.of("USER_ROLE"));

        when(userRepository.findByName(userNameToFind)).thenReturn(Optional.of(mockUser));
        when(userRepository.save(mockUser)).thenReturn(mockUser);

        UserResponse.UserDetails response = userService.updateUserDetailsByName(request);

        verify(userRepository).save(mockUser);

        assertTrue(response.name().contains(userName));
        assertTrue(response.email().contains(userEmail));
        assertTrue(response.roles().contains(userRole));

    }

    @Test
    void updateUserDetailsByEmail(){

        Long userId = 1L;
        String userEmailToFind = "test@example.com";
        String userName = "Example Test";
        String userEmail = "test@test.com";
        String userRole = "ROLE_USER";

        UserRequest.UpdateByEmail request = new UserRequest.UpdateByEmail(
                userEmailToFind,
                userEmail,
                userName,
                userRole
        );

        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setName("test");
        mockUser.setEmail(userEmailToFind);
        mockUser.setRoles(List.of("USER_ROLE"));

        when(userRepository.findByEmail(userEmailToFind)).thenReturn(Optional.of(mockUser));
        when(userRepository.save(mockUser)).thenReturn(mockUser);

        UserResponse.UserDetails response = userService.updateUserDetailsByEmail(request);

        verify(userRepository).save(mockUser);

        assertTrue(response.name().contains(userName));
        assertTrue(response.email().contains(userEmail));
        assertTrue(response.roles().contains(userRole));

    }

    @Test
    void getAllUsers_success() {

        List<User> mockUsers = new ArrayList<>();

        for (int i = 1; i <= 3; i++) {
            User mockUser = new User();
            mockUser.setId((long) i);
            mockUser.setName("User " + i);
            mockUser.setEmail("user" + i + "@example.com");
            mockUsers.add(mockUser);
        }

        Pageable pageable = PageRequest.of(0, 10);
        Page<User> mockPage = new PageImpl<>(mockUsers);

        when(userRepository.findAll(pageable)).thenReturn(mockPage);

        Page<UserResponse.UserDetails> response = userService.getAllUsers(pageable);

        verify(userRepository).findAll(pageable);

        assertEquals(mockUsers.size(), response.getContent().size());
        for (int i = 0; i < mockUsers.size(); i++) {
            assertEquals(mockUsers.get(i).getId(), response.getContent().get(i).id());
            assertEquals(mockUsers.get(i).getName(), response.getContent().get(i).name());
            assertEquals(mockUsers.get(i).getEmail(), response.getContent().get(i).email());
        }
    }

    @Test
    void getUserById_success(){

        Long userId = 1L;

        User mockUser = new User();
        mockUser.setId(userId);

        when(userRepository.findById(userId)).thenReturn(Optional.of(mockUser));

        UserResponse.UserDetails response = userService.getUserById(userId);

        verify(userRepository).findById(userId);

        assertEquals(userId, response.id());

    }

    @Test
    void getUserByName_success(){

        Long userId = 1L;
        String userName = "Example Test";

        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setName(userName);

        when(userRepository.findByName(userName)).thenReturn(Optional.of(mockUser));

        UserResponse.UserDetails response = userService.getUserByName(userName);

        verify(userRepository).findByName(userName);

        assertEquals(userName, response.name());

    }

    @Test
    void getUserByEmail_success(){

        Long userId = 1L;
        String userEmail = "test@example.com";

        User mockUser = new User();
        mockUser.setId(userId);
        mockUser.setEmail(userEmail);

        when(userRepository.findByEmail(userEmail)).thenReturn(Optional.of(mockUser));

        UserResponse.UserDetails response = userService.getUserByEmail(userEmail);

        verify(userRepository).findByEmail(userEmail);

        assertEquals(userEmail, response.email());

    }

    @Test
    void userExistByEmail() {

        String userEmail = "test@example.com";

        when(userRepository.existsByEmail(userEmail)).thenReturn(true);

        boolean response = userService.userExistsByEmail(userEmail);

        verify(userRepository).existsByEmail(userEmail);

        assertTrue(response);
    }

}
