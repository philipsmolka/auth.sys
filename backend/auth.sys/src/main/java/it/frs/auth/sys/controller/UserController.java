package it.frs.auth.sys.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import it.frs.auth.sys.dto.UserRequest;
import it.frs.auth.sys.dto.UserResponse;
import it.frs.auth.sys.service.UserService;

/**
 * REST controller for managing users.
 * <p>
 * Exposes endpoints for user registration, deletion, password change,
 * editing, and retrieval. Uses sealed DTOs for request and response types
 * to maintain strong typing and clear API contracts.
 */
@CrossOrigin(origins = "http://localhost:3000")
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    /**
     * Adds a new user.
     *
     * @param request user creation request containing email, password, name, and roles
     * @return response entity with created user details
     */
    @PostMapping
    public ResponseEntity<UserResponse.UserDetails> add(@Valid @RequestBody UserRequest.Add request) {
        UserResponse.UserDetails response = userService.addUser(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }//DONE

    /**
     * Deletes a user by id.
     *
     * @param id the id of the user to delete
     * @return response entity indicating success
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<UserResponse.SuccessMessage> deleteUser(@PathVariable Long id) {
        UserResponse.SuccessMessage response = userService.deleteUserById(id);
        return ResponseEntity.ok(response);
    }//DONE

    /**
     * Changes the password of a user by ID.
     *
     * @param request the request containing the user ID and new password
     * @return response entity indicating success of the password change
     */
    @PatchMapping("/{id}/password")
    public ResponseEntity<UserResponse.SuccessMessage> changePassword(
            @PathVariable Long id,
            @Valid @RequestBody UserRequest.ChangePassword request) {
        UserResponse.SuccessMessage response = userService.changeUserPasswordById(id, request);
        return ResponseEntity.ok(response);
    }//DONE

    /**
     * Disables a user by ID.
     * Marks the user as inactive without deleting them from the database.
     *
     * @param id the ID of the user to disable
     * @return response entity containing the updated user details
     */
    @PatchMapping("/{id}/disable")
    public ResponseEntity<UserResponse.UserDetails> disableUser(@PathVariable Long id) {
        UserResponse.UserDetails response = userService.disableUserById(id);
        return ResponseEntity.ok(response);
    }//DONE

    /**
     * Enables a user by ID.
     * Marks the user as active..
     *
     * @param id the ID of the user to enable
     * @return response entity containing the updated user details
     */
    @PatchMapping("/{id}/enable")
    public ResponseEntity<UserResponse.UserDetails> enableUser(@PathVariable Long id) {
        UserResponse.UserDetails response = userService.enableUserById(id);
        return ResponseEntity.ok(response);
    }

    /**
     * Disables a user by ID.
     * Marks the user as inactive without deleting them from the database.
     *
     * @param id the ID of the user to disable
     * @return response entity containing the updated user details
     */
    @PatchMapping("/{id}/role")
    public ResponseEntity<UserResponse.UserDetails> changeRole(@PathVariable Long id, @Valid UserRequest.ChangeRole request) {
        UserResponse.UserDetails response = userService.changeUserRoleById(id, request);
        return ResponseEntity.ok(response);
    }//DONE

    /**
     * Updates user details identified by ID.
     *
     * @param request update request containing new email, name, roles, etc.
     * @return response entity with updated user details
     */
    @PutMapping("/{id}")
    public ResponseEntity<UserResponse.UserDetails> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserRequest.Update request) {
        UserResponse.UserDetails response = userService.updateUserDetailsById(id, request);
        return ResponseEntity.ok(response);
    }//DONE

    /**
     * Retrieves a paginated list of users.
     *
     * @param pageable pagination and sorting information
     * @return response entity containing a page of user details
     */
    @GetMapping
    public ResponseEntity<Page<UserResponse.UserDetails>> getUsers(Pageable pageable) {
        Page<UserResponse.UserDetails> response = userService.getAllUsers(pageable);
        return ResponseEntity.ok(response);
    }//DONE

    /**
     * Retrieves a user by their unique ID.
     *
     * @param id the user ID
     * @return response entity with user details
     */
    @GetMapping("/{id}")
    public ResponseEntity<UserResponse.UserDetails> getUserById(@PathVariable Long id) {
        UserResponse.UserDetails details = userService.getUserById(id);
        return ResponseEntity.ok(details);
    }//DONE
}
