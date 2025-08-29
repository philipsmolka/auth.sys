package it.frs.auth.sys.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

/**
 * Service responsible for sending application-related emails.
 * All email sending operations are executed asynchronously to avoid blocking the main application thread.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    @Value("${app.frontend.url}")
    private String frontendUrl;

    private final JavaMailSender mailSender;

    /**
     * Asynchronously sends a password reset email to the specified address.
     * The email contains a unique link for the user to reset their password.
     *
     * @param to        The recipient's email address.
     * @param resetLink The full, unique URL for the password reset page.
     */
    @Async
    public void sendResetPassword(String to, String resetLink) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("FRS");
        message.setText(
                "Click link to reset your password:\n" + resetLink
        );
        mailSender.send(message);
        log.info("Password reset email sent to {}", to);
    }

    /**
     * Asynchronously sends an account confirmation email.
     * The email contains a link with a token to activate the user's account.
     *
     * @param toEmail The recipient's email address.
     * @param token   The confirmation token to be included in the activation link.
     */
    @Async
    public void sendConfirmationEmail(String toEmail, String token) {
        String link = frontendUrl + "/api/auth/confirm?token=" + token;
        String subject = "FRS";
        String body = "Click link to enable account:\n" + link;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
        log.info("Account confirmation email sent to {}", toEmail);
    }

    /**
     * Asynchronously sends a notification about successful account registration or activation.
     *
     * @param toEmail The recipient's email address.
     */
    @Async
    public void sendAccountEnableEmail(String toEmail) {
        String subject = "FRS";
        String body = "Your account has been enabled.";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
        log.info("Account enabled notification sent to {}", toEmail);
    }

    /**
     * Asynchronously sends a notification that the user's account has been banned.
     *
     * @param toEmail The recipient's email address.
     */
    @Async
    public void sendAccountBannedEmail(String toEmail) {
        String subject = "FRS";
        String body = "Your account has been banned!";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
        log.info("Account banned notification sent to {}", toEmail);
    }

    /**
     * Asynchronously sends a generic warning or notification email to the user.
     *
     * @param toEmail The recipient's email address.
     */
    @Async
    public void sendAccountWarnEmail(String toEmail) {
        String subject = "FRS";
        String body = "Your account has been warned!";

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(toEmail);
        message.setSubject(subject);
        message.setText(body);
        mailSender.send(message);
        log.info("Account warning email sent to {}", toEmail);
    }
}