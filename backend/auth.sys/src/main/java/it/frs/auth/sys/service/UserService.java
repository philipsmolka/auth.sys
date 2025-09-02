package it.frs.auth.sys.service;

import it.frs.auth.sys.exception.*;
import jakarta.validation.constraints.Email;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import it.frs.auth.sys.dto.UserRequest;
import it.frs.auth.sys.dto.UserResponse;
import it.frs.auth.sys.model.User;
import it.frs.auth.sys.repository.UserRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Service layer for managing users in the authentication system.
 * Provides methods for creating, updating, deleting, disabling users,
 * changing their passwords and roles, and fetching user details.
 *
 * Uses {@link UserRepository} for persistence and {@link PasswordEncoder} for encoding passwords.
 * Includes role-based checks to prevent modification of administrator accounts.
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
@SuppressWarnings("unused")
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    private final EmailService emailService;
    private final JwtService jwtService;

    private static final Set<String> ALLOWED_ROLES = Set.of("ROLE_USER", "ROLE_MODERATOR");
    private static final String ADMIN_ROLE = "ROLE_ADMIN";

    //region Helper Methods

    /**
     * Converts a {@link User} entity to a {@link UserResponse.UserDetails} DTO.
     *
     * @param user the user entity to convert
     * @return DTO containing user's public details like id, email, name, roles, and active status
     */
    private UserResponse.UserDetails toUserDetails(User user) {
        return new UserResponse.UserDetails(user.getId(), user.getEmail(), user.getName(), user.getRoles(), user.isEnabled());
    }//DONE

    /**
     * Finds a user by their unique ID.
     *
     * @param id the unique identifier of the user
     * @return the found {@link User} entity
     * @throws ResourceNotFoundException if no user is found with the given ID
     */
    private User findUserByIdOrThrow(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User with ID " + id + " not found."));
    }//DONE

    /**
     * Finds a user by their email.
     *
     * @param email the email address of the user
     * @return the found {@link User} entity
     * @throws ResourceNotFoundException if no user is found with the given email
     */
    private User findUserByEmailOrThrow(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User with email " + email + " not found."));
    }//DONE

    /**
     * Finds a user by their username.
     *
     * @param name the username of the user
     * @return the found {@link User} entity
     * @throws ResourceNotFoundException if no user is found with the given name
     */
    private User findUserByNameOrThrow(String name) {
        return userRepository.findByName(name)
                .orElseThrow(() -> new ResourceNotFoundException("User with name " + name + " not found."));
    }//DONE

    /**
     * Checks whether the user has the administrator role.
     *
     * Throws an {@link OperationForbiddenException} if the user is an administrator,
     * to prevent modification or deletion of admin accounts.
     *
     * @param user the user to check
     * @throws OperationForbiddenException if user is an administrator
     */
    private void checkAdminModification(User user) {
        if (user.getRoles() != null && user.getRoles().contains(ADMIN_ROLE)) {
            throw new OperationForbiddenException("Administrator account cannot be modified or deleted.");
        }
    }//DONE

    /**
     * Performs the actual update of a user's details (name, email, role).
     * Validates email uniqueness and role correctness.
     *
     * @param user the user entity to update
     * @param newName the new username
     * @param newEmail the new email address
     * @param newRole the new role to assign
     * @return updated {@link UserResponse.UserDetails} DTO
     * @throws ResourceAlreadyExistsException if newEmail is already in use
     * @throws InvalidInputException if newRole is not allowed
     */
    private UserResponse.UserDetails performUserDetailsUpdate(User user, String newName, String newEmail, String newRole) {

        String oldUserName = user.getName();
        String oldUserEmail = user.getEmail();
        List<String> oldUserRoles = user.getRoles();

        checkAdminModification(user);
        if (!user.getEmail().equals(newEmail) && userRepository.existsByEmail(newEmail)) {
            throw new ResourceAlreadyExistsException("Email " + newEmail + " is already taken.");
        }
        if (!ALLOWED_ROLES.contains(newRole)) {
            throw new InvalidInputException("Invalid role provided: " + newRole);
        }
        user.setName(newName);
        user.setEmail(newEmail);
        user.setRoles(new ArrayList<>(List.of(newRole)));
        User updatedUser = userRepository.save(user);
        log.info(
                "Updated details for user with ID: {} Name: {} => {} Email: {} => {} Roles: {} => {}",
                user.getId(), oldUserName, newName,
                oldUserEmail, newEmail,
                oldUserRoles, List.of(newRole)
        );
        return toUserDetails(updatedUser);
    }//DONE

    //endregion

    //region Main Public Methods

    /**
     * Creates and saves a new user with the provided details.
     *
     * @param request DTO containing email, name, password, and role of the new user
     * @return the details of the newly created user as a DTO
     * @throws ResourceAlreadyExistsException if a user with the given email already exists
     * @throws InvalidInputException if the provided role is not allowed
     */
    @Transactional
    public UserResponse.UserDetails addUser(UserRequest.Add request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new ResourceAlreadyExistsException("A user with this email address already exists.");
        }
        if (!ALLOWED_ROLES.contains(request.role())) {
            throw new InvalidInputException("Invalid role provided. Allowed roles: " + ALLOWED_ROLES);
        }

        User newUser = new User();
        newUser.setEmail(request.email());
        newUser.setName(request.name());
        newUser.setPassword(passwordEncoder.encode(request.password()));
        newUser.setRoles(List.of(request.role()));
        newUser.setActive(true);

        User savedUser = userRepository.save(newUser);
        log.info("Added a new user: {}", savedUser.getEmail());
        return toUserDetails(savedUser);
    }//DONE

    /**
     * Deletes a user identified by their ID.
     *
     * @param id containing the user's ID
     * @throws ResourceNotFoundException if user not found
     * @throws OperationForbiddenException if user is an administrator
     */
    @Transactional
    public UserResponse.SuccessMessage deleteUserById(Long id) {
        User user = findUserByIdOrThrow(id);
        checkAdminModification(user);
        userRepository.delete(user);
        log.info("Deleted user with ID: {}", id.toString());

        return new UserResponse.SuccessMessage("User deleted successfully.");
    }//DONE

    /**
     * Deletes a user identified by their username.
     *
     * @param name containing the user's name
     * @throws ResourceNotFoundException if user with given name not found
     * @throws OperationForbiddenException if user is an administrator
     */
    @Transactional
    public UserResponse.SuccessMessage deleteUserByName(String name) {
        User user = findUserByNameOrThrow(name);
        checkAdminModification(user);
        userRepository.delete(user);
        log.info("Deleted user with name: {}", name);
        return new UserResponse.SuccessMessage("User deleted successfully.");
    }//DONE

    /**
     * Deletes a user identified by their email address.
     *
     * @param email containing the user's email
     * @throws ResourceNotFoundException if user not found
     * @throws OperationForbiddenException if user is an administrator
     */
    @Transactional
    public UserResponse.SuccessMessage deleteUserByEmail(String email) {
        User user = findUserByEmailOrThrow(email);
        checkAdminModification(user);
        userRepository.delete(user);
        log.info("Deleted user with email: {}", email);
        return new UserResponse.SuccessMessage("User deleted successfully.");
    }//DONE

    /**
     * Disables a user account by their ID.
     *
     * @param id containing the user's ID
     * @throws ResourceNotFoundException if user not found
     * @throws OperationForbiddenException if user is an administrator
     */
    @Transactional
    public UserResponse.UserDetails disableUserById(Long id) {
        User user = findUserByIdOrThrow(id);
        if(!user.isEnabled()){
            throw new ResourceAlreadyExistsException("User already disabled");
        }
        checkAdminModification(user);
        user.setActive(false);
        userRepository.save(user);
        log.info("Disabled user with ID: {}", id);
        return toUserDetails(user);
    }//DONE

    /**
     * Disables a user account by their username.
     *
     * @param name containing the user's name
     * @throws ResourceNotFoundException if user not found
     * @throws OperationForbiddenException if user is an administrator
     */
    @Transactional
    public UserResponse.UserDetails disableUserByName(String name) {
        User user = findUserByNameOrThrow(name);
        checkAdminModification(user);
        user.setActive(false);
        userRepository.save(user);
        log.info("Disabled user with name: {}", name);
        return toUserDetails(user);
    }//DONE

    /**
     * Disables a user account by their email.
     *
     * @param email containing the user's email
     * @throws ResourceNotFoundException if user not found
     * @throws OperationForbiddenException if user is an administrator
     */
    @Transactional
    public UserResponse.UserDetails disableUserByEmail(String email) {
        User user = findUserByEmailOrThrow(email);
        checkAdminModification(user);
        user.setActive(false);
        userRepository.save(user);
        log.info("Disabled user with email: {}", email);
        return toUserDetails(user);
    }//DONE

    /**
     * Enables a user account by their id.
     *
     * @param id containing the user's id
     * @throws ResourceNotFoundException if user not found
     * @throws OperationForbiddenException if user is an administrator
     */
    @Transactional
    public UserResponse.UserDetails enableUserById(Long id){
        User user = findUserByIdOrThrow(id);
        if(user.isEnabled()){
            throw new ResourceAlreadyExistsException("User already enabled.");
        }
        checkAdminModification(user);
        user.setActive(true);
        userRepository.save(user);
        log.info("Enabled user with id: {}", id);
        return toUserDetails(user);
    }//DONE

    /**
     * Enables a user account by their username.
     *
     * @param name containing the user's name
     * @throws ResourceNotFoundException if user not found
     * @throws OperationForbiddenException if user is an administrator
     */
    @Transactional
    public UserResponse.UserDetails enableUserByName(String name) {
        User user = findUserByNameOrThrow(name);
        checkAdminModification(user);
        user.setActive(true);
        userRepository.save(user);
        log.info("Enabled user with name: {}", name);
        return toUserDetails(user);
    }//DONE

    /**
     * Enables a user account by their email.
     *
     * @param email containing the user's email
     * @throws ResourceNotFoundException if user not found
     * @throws OperationForbiddenException if user is an administrator
     */
    @Transactional
    public UserResponse.UserDetails enableUserByEmail(String email) {
        User user = findUserByEmailOrThrow(email);
        checkAdminModification(user);
        user.setActive(true);
        userRepository.save(user);
        log.info("Enabled user with email: {}", email);
        return toUserDetails(user);
    }//DONE

    /**
     * Changes a user's password identified by their ID.
     *
     * @param request DTO containing user's ID and new password
     * @return success message DTO
     * @throws ResourceNotFoundException if user not found
     * @throws OperationForbiddenException if user is an administrator
     */
    @Transactional
    public UserResponse.SuccessMessage changeUserPasswordById(Long id ,UserRequest.ChangePassword request) {
        User user = findUserByIdOrThrow(id);
        checkAdminModification(user);
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        User savedUser = userRepository.save(user);
        log.info("Password changed for user with ID: {}", id);
        return new UserResponse.SuccessMessage("Password updated successfully.");
    }//DONE

    /**
     * Changes a user's password identified by their username.
     *
     * @param request DTO containing user's name and new password
     * @return success message DTO
     * @throws ResourceNotFoundException if user not found
     * @throws OperationForbiddenException if user is an administrator
     */
    @Transactional
    public UserResponse.SuccessMessage changeUserPasswordByName(UserRequest.ChangePasswordByName request) {
        User user = findUserByNameOrThrow(request.name());
        checkAdminModification(user);
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        log.info("Password changed for user with name: {}", request.name());
        return new UserResponse.SuccessMessage("Password updated successfully.");
    }//DONE

    /**
     * Changes a user's password identified by their email.
     *
     * @param request DTO containing user's email and new password
     * @return success message DTO
     * @throws ResourceNotFoundException if user not found
     * @throws OperationForbiddenException if user is an administrator
     */
    @Transactional
    public UserResponse.SuccessMessage changeUserPasswordByEmail(UserRequest.ChangePasswordByEmail request) {
        User user = findUserByEmailOrThrow(request.email());
        checkAdminModification(user);
        user.setPassword(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);
        log.info("Password changed for user with email: {}", request.email());
        return new UserResponse.SuccessMessage("Password updated successfully.");
    }//DONE

    /**
     * Changes the role of a user identified by their ID.
     *
     * @param request DTO containing user's ID and new role
     * @return updated user details DTO
     * @throws ResourceNotFoundException if user not found
     * @throws OperationForbiddenException if user is an administrator
     * @throws InvalidInputException if the new role is invalid
     */
    @Transactional
    public UserResponse.UserDetails changeUserRoleById(Long id, UserRequest.ChangeRole request) {
        User user = findUserByIdOrThrow(id);
        checkAdminModification(user);

        String newRole = request.newRole();
        if (!ALLOWED_ROLES.contains(newRole)) {
            throw new InvalidInputException("Invalid role: " + newRole);
        }

        user.setRoles(new ArrayList<>(List.of(newRole)));
        User updatedUser = userRepository.save(user);
        log.info("Changed role for user with ID: {}", id);
        return toUserDetails(updatedUser);
    }//DONE

    /**
     * Changes the role of a user identified by their username.
     *
     * @param request DTO containing user's name and new role
     * @return updated user details DTO
     * @throws ResourceNotFoundException if user not found
     * @throws OperationForbiddenException if user is an administrator
     * @throws InvalidInputException if the new role is invalid
     */
    @Transactional
    public UserResponse.UserDetails changeUserRoleByName(UserRequest.ChangeRoleByName request) {
        User user = findUserByNameOrThrow(request.name());
        checkAdminModification(user);

        String newRole = request.newRole();
        if (!ALLOWED_ROLES.contains(newRole)) {
            throw new InvalidInputException("Invalid role: " + newRole);
        }

        user.setRoles(List.of(newRole));
        User updatedUser = userRepository.save(user);
        log.info("Changed role for user with name: {}", request.name());
        return toUserDetails(updatedUser);
    }//DONE

    /**
     * Changes the role of a user identified by their email.
     *
     * @param request DTO containing user's email and new role
     * @return updated user details DTO
     * @throws ResourceNotFoundException if user not found
     * @throws OperationForbiddenException if user is an administrator
     * @throws InvalidInputException if the new role is invalid
     */
    @Transactional
    public UserResponse.UserDetails changeUserRoleByEmail(UserRequest.ChangeRoleByEmail request) {
        User user = findUserByEmailOrThrow(request.email());
        checkAdminModification(user);

        String newRole = request.newRole();
        if (!ALLOWED_ROLES.contains(newRole)) {
            throw new InvalidInputException("Invalid role: " + newRole);
        }

        user.setRoles(List.of(newRole));
        User updatedUser = userRepository.save(user);
        log.info("Changed role for user with email: {}", request.email());
        return toUserDetails(updatedUser);
    }//DONE

    /**
     * Updates user's name, email and role based on their ID.
     *
     * @param request DTO containing user's ID and new details
     * @return updated user details DTO
     * @throws ResourceNotFoundException if user not found
     * @throws ResourceAlreadyExistsException if new email is already used by another user
     * @throws OperationForbiddenException if user is an administrator
     * @throws InvalidInputException if new role is invalid
     */
    @Transactional
    public UserResponse.UserDetails updateUserDetailsById(Long id, UserRequest.Update request) {
        User user = findUserByIdOrThrow(id);
        return performUserDetailsUpdate(user, request.name(), request.email(), request.role());
    }//DONE

    /**
     * Updates user's name, email and role based on their username.
     *
     * @param request DTO containing user's name and new details
     * @return updated user details DTO
     * @throws ResourceNotFoundException if user not found
     * @throws ResourceAlreadyExistsException if new email is already used by another user
     * @throws OperationForbiddenException if user is an administrator
     * @throws InvalidInputException if new role is invalid
     */
    @Transactional
    public UserResponse.UserDetails updateUserDetailsByName(UserRequest.UpdateByName request) {
        User user = findUserByNameOrThrow(request.nameToFind());
        return performUserDetailsUpdate(user, request.name(), request.email(), request.role());
    }//DONE

    /**
     * Updates user's name, email and role based on their email.
     *
     * @param request DTO containing user's email and new details
     * @return updated user details DTO
     * @throws ResourceNotFoundException if user not found
     * @throws ResourceAlreadyExistsException if new email is already used by another user
     * @throws OperationForbiddenException if user is an administrator
     * @throws InvalidInputException if new role is invalid
     */
    @Transactional
    public UserResponse.UserDetails updateUserDetailsByEmail(UserRequest.UpdateByEmail request) {
        User user = findUserByEmailOrThrow(request.emailToFind());
        return performUserDetailsUpdate(user, request.name(), request.email(), request.role());
    }//DONE

    /**
     * Retrieves a paginated list of users with their details.
     *
     * @param pageable pagination information
     * @return a page of user details DTOs
     */
    @Transactional(readOnly = true)
    public Page<UserResponse.UserDetails> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .map(this::toUserDetails);
    }//DONE

    /**
     * Retrieves details of a user by their ID.
     *
     * @param id DTO containing the user's ID
     * @return user details DTO
     * @throws ResourceNotFoundException if user not found
     */
    @Transactional(readOnly = true)
    public UserResponse.UserDetails getUserById(Long id) {
        User user = findUserByIdOrThrow(id);
        return toUserDetails(user);
    }//DONE

    /**
     * Retrieves details of a user by their username.
     *
     * @param name containing the user's name
     * @return user details DTO
     * @throws ResourceNotFoundException if user not found
     */
    @Transactional(readOnly = true)
    public UserResponse.UserDetails getUserByName(String name) {
        User user = findUserByNameOrThrow(name);
        return toUserDetails(user);
    }//DONE

    /**
     * Retrieves details of a user by their email.
     *
     * @param email containing the user's email
     * @return user details DTO
     * @throws ResourceNotFoundException if user not found
     */
    @Transactional(readOnly = true)
    public UserResponse.UserDetails getUserByEmail(String email) {
        User user = findUserByEmailOrThrow(email);
        return toUserDetails(user);
    }//DONE

    /**
     * Checks if a user exists by their email.
     *
     * @param email the email to check
     * @return true if a user exists with this email, false otherwise
     */
    @Transactional(readOnly = true)
    public boolean userExistsByEmail(String email) {
        return userRepository.existsByEmail(email);
    }
    //DONE



}
