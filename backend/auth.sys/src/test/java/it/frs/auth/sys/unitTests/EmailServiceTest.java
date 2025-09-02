package it.frs.auth.sys.unitTests;

import it.frs.auth.sys.service.EmailService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.verify;

public class EmailServiceTest {

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private EmailService emailService;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void sendResetPasswordEmail_sendsEmail(){

        String userEmail = "example@test.com";

        String resetLink = "reset-link";

        emailService.sendResetPassword(userEmail, resetLink);

        verify(mailSender).send(argThat((SimpleMailMessage message) ->
                message.getTo()[0].equals(userEmail) &&
                        message.getSubject().equals("FRS") &&
                        message.getText().contains(resetLink)
        ));

    }

    @Test
    void sendConfirmationEmail_sendsEmail(){

        String userEmail = "example@test.com";

        String token = "confirmation-token";

        emailService.sendConfirmationEmail(userEmail, token);

        verify(mailSender).send(argThat((SimpleMailMessage message) ->
                message.getTo()[0].equals(userEmail) &&
                        message.getSubject().equals("FRS") &&
                        message.getText().contains(token)
        ));

    }

    @Test
    void sendAccountEnableEmail_sendsEmail(){

        String userEmail = "example@test.com";

        emailService.sendAccountEnableEmail(userEmail);

        verify(mailSender).send(argThat((SimpleMailMessage message) ->
                message.getTo()[0].equals(userEmail) &&
                        message.getSubject().equals("FRS") &&
                        message.getText().contains("Your account has been enabled.")
        ));

    }

    @Test
    void sendAccountBannedEmail_sendsEmail(){

        String userEmail = "example@test.com";

        emailService.sendAccountBannedEmail(userEmail);

        verify(mailSender).send(argThat((SimpleMailMessage message) ->
                message.getTo()[0].equals(userEmail) &&
                        message.getSubject().equals("FRS") &&
                        message.getText().contains("Your account has been banned!")
        ));

    }

    @Test
    public void sendAccountWarnEmail(){

        String userEmail = "example@test.com";

        emailService.sendAccountWarnEmail(userEmail);

        verify(mailSender).send(argThat((SimpleMailMessage message) ->
                message.getTo()[0].equals(userEmail) &&
                        message.getSubject().equals("FRS") &&
                        message.getText().contains("Your account has been warned!")
        ));

    }

}
