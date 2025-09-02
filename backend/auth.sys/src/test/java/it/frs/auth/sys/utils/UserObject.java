package it.frs.auth.sys.utils;

import it.frs.auth.sys.model.User;
import jakarta.validation.constraints.Email;

import java.util.List;

public class UserObject {

    public static User testUser() {
        User user = new User();
        user.setEmail("example@user.com");
        user.setName("example-user");
        user.setPassword("example-password"); // jeśli masz encoder, możesz też hashować
        user.setRoles(List.of("ROLE_USER"));
        user.setActive(true);
        return user;
    }

    public static User testUserFactory(@Email String email, String name) {
        User user = new User();
        user.setEmail(email);
        user.setName(name);
        user.setPassword("example-password"); // jeśli masz encoder, możesz też hashować
        user.setRoles(List.of("ROLE_USER"));
        user.setActive(true);
        return user;
    }

    public static User testAdminUser() {
        User user = new User();
        user.setEmail("example@admin.com");
        user.setName("example-admin");
        user.setPassword("example-admin-password"); // jeśli masz encoder, możesz też hashować
        user.setRoles(List.of("ROLE_ADMIN"));
        user.setActive(true);
        return user;
    }

    public static String testUserJSON() {
        return """
            {
              "email": "example@user.com",
              "name": "example-user",
              "password": "example-password",
              "role": "ROLE_USER"
            }
            """;
    }

}
