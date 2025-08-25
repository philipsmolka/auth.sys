package it.frs.auth.sys.dto;

import jakarta.validation.constraints.*;

/**
 * Sealed interface representing various types of user-related requests.
 * <p>
 * This interface permits only specific record implementations representing different
 * user operations such as adding, registering, deleting, disabling, changing password,
 * role, name, email, or updating users.
 * <p>
 * Validation messages can be externalized for localization (i18n).
 */
public sealed interface UserRequest permits

        UserRequest.Add,
        UserRequest.Register,

        UserRequest.Delete,
        UserRequest.DeleteByName,
        UserRequest.DeleteByEmail,

        UserRequest.Disable,
        UserRequest.DisableByName,
        UserRequest.DisableByEmail,

        UserRequest.ChangePassword,
        UserRequest.ChangePasswordByName,
        UserRequest.ChangePasswordByEmail,

        UserRequest.ChangeRole,
        UserRequest.ChangeRoleByName,
        UserRequest.ChangeRoleByEmail,

        UserRequest.ChangeName,
        UserRequest.ChangeNameByName,
        UserRequest.ChangeNameByEmail,

        UserRequest.ChangeEmail,
        UserRequest.ChangeEmailByName,
        UserRequest.ChangeEmailByEmail,

        UserRequest.Update,
        UserRequest.UpdateByName,
        UserRequest.UpdateByEmail,

        UserRequest.Get,
        UserRequest.GetByName,
        UserRequest.GetByEmail


{

    // Validation messages – can be externalized to a properties file for localization (i18n)
    String ID_CANNOT_BE_NULL = "ID must not be null.";
    String EMAIL_CANNOT_BE_BLANK = "Email must not be blank.";
    String INVALID_EMAIL_FORMAT = "Invalid email format.";
    String PASSWORD_CANNOT_BE_BLANK = "Password must not be blank.";
    String PASSWORD_SIZE_MESSAGE = "Password must be between 8 and 100 characters.";
    String NAME_CANNOT_BE_BLANK = "Username must not be blank.";
    String ROLE_CANNOT_BE_BLANK = "Role must not be blank.";

    // --- User creation ---

    /**
     * DTO for creating a new user (admin use).
     *
     * @param email    user's email, must be a valid email and not blank
     * @param password user's password, must be between 8 and 100 characters and not blank
     * @param name     user's name, must not be blank
     * @param role     user's role, must not be blank
     */
    record Add(
            @NotBlank(message = EMAIL_CANNOT_BE_BLANK) @Email(message = INVALID_EMAIL_FORMAT) String email,
            @NotBlank(message = PASSWORD_CANNOT_BE_BLANK) @Size(min = 8, max = 100, message = PASSWORD_SIZE_MESSAGE) String password,
            @NotBlank(message = NAME_CANNOT_BE_BLANK) String name,
            @NotBlank(message = ROLE_CANNOT_BE_BLANK) String role
    ) implements UserRequest {}

    /**
     * DTO for self-registration (public use).
     *
     * @param email    user's email, must be a valid email and not blank
     * @param password user's password, must be between 8 and 100 characters and not blank
     * @param name     user's name, must not be blank
     */
    record Register(
            @NotBlank(message = EMAIL_CANNOT_BE_BLANK) @Email(message = INVALID_EMAIL_FORMAT) String email,
            @NotBlank(message = PASSWORD_CANNOT_BE_BLANK) @Size(min = 8, max = 100, message = PASSWORD_SIZE_MESSAGE) String password,
            @NotBlank(message = NAME_CANNOT_BE_BLANK) String name
    ) implements UserRequest {}

    // --- User deletion ---

    /**
     * DTO to delete a user by their ID.
     *
     * @param id user's unique identifier, must not be null
     */
    record Delete(@NotNull(message = ID_CANNOT_BE_NULL) Long id) implements UserRequest {}

    /**
     * DTO to delete a user by their name.
     *
     * @param name user's name, must not be blank
     */
    record DeleteByName(@NotBlank(message = NAME_CANNOT_BE_BLANK) String name) implements UserRequest {}

    /**
     * DTO to delete a user by their email.
     *
     * @param email user's email, must be a valid email and not blank
     */
    record DeleteByEmail(@NotBlank(message = EMAIL_CANNOT_BE_BLANK) @Email(message = INVALID_EMAIL_FORMAT) String email) implements UserRequest {}

    // --- Disable user ---

    /**
     * DTO to disable a user by their ID.
     *
     * @param id user's unique identifier, must not be null
     */
    record Disable(@NotNull(message = ID_CANNOT_BE_NULL) Long id) implements UserRequest {}

    /**
     * DTO to disable a user by their name.
     *
     * @param name user's name, must not be blank
     */
    record DisableByName(@NotBlank(message = NAME_CANNOT_BE_BLANK) String name) implements UserRequest {}

    /**
     * DTO to disable a user by their email.
     *
     * @param email user's email, must be a valid email and not blank
     */
    record DisableByEmail(@NotBlank(message = EMAIL_CANNOT_BE_BLANK) @Email(message = INVALID_EMAIL_FORMAT) String email) implements UserRequest {}

    // --- Change password ---

    /**
     * DTO to change a user's password by their ID.
     *
     * @param newPassword new password, must be between 8 and 100 characters and not blank
     */
    record ChangePassword(
            @NotBlank(message = PASSWORD_CANNOT_BE_BLANK) @Size(min = 8, max = 100, message = PASSWORD_SIZE_MESSAGE) String newPassword
    ) implements UserRequest {}

    /**
     * DTO to change a user's password by their name.
     *
     * @param name        user's name, must not be blank
     * @param newPassword new password, must be between 8 and 100 characters and not blank
     */
    record ChangePasswordByName(
            @NotBlank(message = NAME_CANNOT_BE_BLANK) String name,
            @NotBlank(message = PASSWORD_CANNOT_BE_BLANK) @Size(min = 8, max = 100, message = PASSWORD_SIZE_MESSAGE) String newPassword
    ) implements UserRequest {}

    /**
     * DTO to change a user's password by their email.
     *
     * @param email       user's email, must be a valid email and not blank
     * @param newPassword new password, must be between 8 and 100 characters and not blank
     */
    record ChangePasswordByEmail(
            @NotBlank(message = EMAIL_CANNOT_BE_BLANK) @Email(message = INVALID_EMAIL_FORMAT) String email,
            @NotBlank(message = PASSWORD_CANNOT_BE_BLANK) @Size(min = 8, max = 100, message = PASSWORD_SIZE_MESSAGE) String newPassword
    ) implements UserRequest {}

    // --- Change role ---

    /**
     * DTO to change a user's role by their ID.
     *
     * @param newRole new role, must not be blank
     */
    record ChangeRole(
            @NotBlank(message = ROLE_CANNOT_BE_BLANK) String newRole
    ) implements UserRequest {}

    /**
     * DTO to change a user's role by their name.
     *
     * @param name    user's name, must not be blank
     * @param newRole new role, must not be blank
     */
    record ChangeRoleByName(
            @NotBlank(message = NAME_CANNOT_BE_BLANK) String name,
            @NotBlank(message = ROLE_CANNOT_BE_BLANK) String newRole
    ) implements UserRequest {}

    /**
     * DTO to change a user's role by their email.
     *
     * @param email   user's email, must be a valid email and not blank
     * @param newRole new role, must not be blank
     */
    record ChangeRoleByEmail(
            @NotBlank(message = EMAIL_CANNOT_BE_BLANK) @Email(message = INVALID_EMAIL_FORMAT) String email,
            @NotBlank(message = ROLE_CANNOT_BE_BLANK) String newRole
    ) implements UserRequest {}

    // --- Change name ---

    /**
     * DTO to change a user's name by their ID.
     *
     * @param newName new username, must not be blank
     */
    record ChangeName(
            @NotBlank(message = NAME_CANNOT_BE_BLANK) String newName
    ) implements UserRequest {}

    /**
     * DTO to change a user's name by their current name.
     *
     * @param oldName current username, must not be blank
     * @param newName new username, must not be blank
     */
    record ChangeNameByName(
            @NotBlank(message = NAME_CANNOT_BE_BLANK) String oldName,
            @NotBlank(message = NAME_CANNOT_BE_BLANK) String newName
    ) implements UserRequest {}

    /**
     * DTO to change a user's name by their email.
     *
     * @param email   user's email, must be a valid email and not blank
     * @param newName new username, must not be blank
     */
    record ChangeNameByEmail(
            @NotBlank(message = EMAIL_CANNOT_BE_BLANK) @Email(message = INVALID_EMAIL_FORMAT) String email,
            @NotBlank(message = NAME_CANNOT_BE_BLANK) String newName
    ) implements UserRequest {}

    // --- Change email ---

    /**
     * DTO to change a user's email by their ID.
     *
     * @param newEmail new email, must be a valid email and not blank
     */
    record ChangeEmail(
            @NotBlank(message = EMAIL_CANNOT_BE_BLANK) @Email(message = INVALID_EMAIL_FORMAT) String newEmail
    ) implements UserRequest {}

    /**
     * DTO to change a user's email by their name.
     *
     * @param name     user's name, must not be blank
     * @param newEmail new email, must be a valid email and not blank
     */
    record ChangeEmailByName(
            @NotBlank(message = NAME_CANNOT_BE_BLANK) String name,
            @NotBlank(message = EMAIL_CANNOT_BE_BLANK) @Email(message = INVALID_EMAIL_FORMAT) String newEmail
    ) implements UserRequest {}

    /**
     * DTO to change a user's email by their current email.
     *
     * @param oldEmail current email, must be a valid email and not blank
     * @param newEmail new email, must be a valid email and not blank
     */
    record ChangeEmailByEmail(
            @NotBlank(message = EMAIL_CANNOT_BE_BLANK) @Email(message = INVALID_EMAIL_FORMAT) String oldEmail,
            @NotBlank(message = EMAIL_CANNOT_BE_BLANK) @Email(message = INVALID_EMAIL_FORMAT) String newEmail
    ) implements UserRequest {}

    // --- Full update ---

    /**
     * DTO to update all user details by their ID.
     *
     * @param email new email, must be a valid email and not blank
     * @param name  new username, must not be blank
     * @param role  new role, must not be blank
     */
    record Update(
            @NotBlank(message = EMAIL_CANNOT_BE_BLANK) @Email(message = INVALID_EMAIL_FORMAT) String email,
            @NotBlank(message = NAME_CANNOT_BE_BLANK) String name,
            @NotBlank(message = ROLE_CANNOT_BE_BLANK) String role
    ) implements UserRequest {}

    /**
     * DTO to update all user details by their name.
     *
     * @param nameToFind name of the user to update, must not be blank
     * @param email      new email, must be a valid email and not blank
     * @param name       new username, must not be blank
     * @param role       new role, must not be blank
     */
    record UpdateByName(
            @NotBlank(message = NAME_CANNOT_BE_BLANK) String nameToFind,
            @NotBlank(message = EMAIL_CANNOT_BE_BLANK) @Email(message = INVALID_EMAIL_FORMAT) String email,
            @NotBlank(message = NAME_CANNOT_BE_BLANK) String name,
            @NotBlank(message = ROLE_CANNOT_BE_BLANK) String role
    ) implements UserRequest {}

    /**
     * DTO to update all user details by their email.
     *
     * @param emailToFind email of the user to update, must be a valid email and not blank
     * @param email       new email, must be a valid email and not blank
     * @param name        new username, must not be blank
     * @param role        new role, must not be blank
     */
    record UpdateByEmail(
            @NotBlank(message = EMAIL_CANNOT_BE_BLANK) @Email(message = INVALID_EMAIL_FORMAT) String emailToFind,
            @NotBlank(message = EMAIL_CANNOT_BE_BLANK) @Email(message = INVALID_EMAIL_FORMAT) String email,
            @NotBlank(message = NAME_CANNOT_BE_BLANK) String name,
            @NotBlank(message = ROLE_CANNOT_BE_BLANK) String role
    ) implements UserRequest {}

    record Get(
            @NotBlank(message = ID_CANNOT_BE_NULL) Long id
    ) implements UserRequest {}

    record GetByName(
            @NotBlank(message = NAME_CANNOT_BE_BLANK) String name
    ) implements UserRequest {}

    record GetByEmail(
            @NotBlank(message = EMAIL_CANNOT_BE_BLANK) @Email(message = INVALID_EMAIL_FORMAT) String email
    ) implements UserRequest {}
}
