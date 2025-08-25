package it.frs.auth.sys.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

/**
 * Entity representing an application user.
 * <p>
 * Implements {@link UserDetails} to integrate with Spring Security.
 * </p>
 *
 * <p>Each user has a unique ID, email, name, encrypted password,
 * a list of assigned roles, and an active status flag.</p>
 *
 * <p>This class is mapped to the {@code users} table in the database.</p>
 */
@Entity
@Table(name = "users")
@SuppressWarnings("all")
public class User implements UserDetails {

    /**
     * Unique identifier of the user.
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(unique = true)
    private Long id;

    /**
     * Unique email address of the user, used as username.
     */
    @Column(unique = true)
    private String email;

    /**
     * Display name of the user.
     */
    private String name;

    /**
     * Encrypted password of the user.
     */
    private String password;

    /**
     * List of roles assigned to the user (e.g., ROLE_USER, ROLE_ADMIN).
     * <p>
     * Roles are eagerly loaded from the database.
     * </p>
     */
    @ElementCollection(fetch = FetchType.EAGER)
    private List<String> roles;

    /**
     * Flag indicating whether the user's account is active/enabled.
     */
    private boolean active;

    /**
     * Returns the authorities granted to the user based on assigned roles.
     *
     * @return a collection of granted authorities.
     */
    @Override
    @JsonIgnore
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return roles.stream()
                .map(SimpleGrantedAuthority::new)
                .toList();
    }

    /**
     * Returns the username used to authenticate the user.
     * <p>
     * In this implementation, the username is the user's email.
     * </p>
     *
     * @return the user's email.
     */
    @Override
    public String getUsername() {
        return email;
    }

    /**
     * Indicates whether the user's account has expired.
     * <p>
     * This implementation always returns {@code true}, meaning
     * accounts never expire.
     * </p>
     *
     * @return {@code true} always.
     */
    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user's account is locked.
     * <p>
     * This implementation always returns {@code true}, meaning
     * accounts are never locked.
     * </p>
     *
     * @return {@code true} always.
     */
    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    /**
     * Indicates whether the user's credentials (password) have expired.
     * <p>
     * This implementation always returns {@code true}, meaning
     * credentials never expire.
     * </p>
     *
     * @return {@code true} always.
     */
    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    /**
     * Indicates whether the user is enabled (active).
     *
     * @return {@code true} if the user is active, {@code false} otherwise.
     */
    @Override
    public boolean isEnabled() {
        return this.active;
    }

    /**
     * Returns the encrypted password of the user.
     *
     * @return the user's password.
     */
    @Override
    public String getPassword() {
        return password;
    }

    /**
     * Returns the unique ID of the user.
     *
     * @return the user ID.
     */
    public Long getId() {
        return id;
    }

    /**
     * Returns the display name of the user.
     *
     * @return the user's name.
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the display name of the user.
     *
     * @param name the new display name.
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the list of roles assigned to the user.
     *
     * @return the list of roles.
     */
    public List<String> getRoles() {
        return roles;
    }

    /**
     * Sets the list of roles assigned to the user.
     *
     * @param roles the new list of roles.
     */
    public void setRoles(List<String> roles) {
        this.roles = roles;
    }

    /**
     * Sets the encrypted password of the user.
     *
     * @param password the new password.
     */
    public void setPassword(String password) {
        this.password = password;
    }

    /**
     * Returns the email address of the user.
     *
     * @return the user's email.
     */
    public String getEmail() {
        return email;
    }

    /**
     * Sets the email address of the user.
     *
     * @param email the new email address.
     */
    public void setEmail(String email) {
        this.email = email;
    }

    /**
     * Sets whether the user's account is active/enabled.
     *
     * @param active {@code true} to activate the account, {@code false} to deactivate it.
     */
    public void setActive(boolean active) {
        this.active = active;
    }
}
