package it.frs.auth.sys.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import it.frs.auth.sys.model.User;

import java.util.Optional;

/**
 * Repository interface for managing {@link User} entities.
 * <p>
 * Provides methods to retrieve, check existence, and delete users
 * by email or username, as well as check for duplicate user entries.
 * </p>
 *
 */
public interface UserRepository extends JpaRepository<User, Long> {

    /**
     * Finds a user by their email address.
     *
     * @param email the email of the user to find.
     * @return an {@link Optional} containing the found user or empty if not found.
     */
    Optional<User> findByEmail(String email);

    /**
     * Checks whether a user with the given email exists.
     *
     * @param email the email to check for.
     * @return {@code true} if a user with the given email exists, otherwise {@code false}.
     */
    boolean existsByEmail(String email);

    /**
     * Deletes a user by their email address.
     *
     * @param email the email of the user to delete.
     */
    void deleteByEmail(String email);

    /**
     * Finds a user by their username.
     *
     * @param name the name of the user to find.
     * @return an {@link Optional} containing the found user or empty if not found.
     */
    Optional<User> findByName(String name);

    /**
     * Checks whether a user with the given username exists.
     *
     * @param name the username to check for.
     * @return {@code true} if a user with the given name exists, otherwise {@code false}.
     */
    boolean existsByName(String name);

    /**
     * Deletes a user by their username.
     *
     * @param name the name of the user to delete.
     */
    void deleteByName(String name);

    /**
     * Checks whether a user exists with the given email or username.
     *
     * @param email the email to check for.
     * @param name  the username to check for.
     * @return {@code true} if a user exists with the given email or name, otherwise {@code false}.
     */
    boolean existsByEmailOrName(String email, String name);
}
