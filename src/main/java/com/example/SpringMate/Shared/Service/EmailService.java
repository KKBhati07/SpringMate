package com.example.SpringMate.Shared.Service;

import com.example.SpringMate.Shared.Constants;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class EmailService {
    private final JavaMailSender javaMailSender;
    private final EmailTemplateService emailTemplateService;

    public void sendOtpEmail(String to, String subject, String otp) throws MessagingException {
        String htmlContent = emailTemplateService.generateOTPEmail(Constants.EmailHeaders.LOGIN, otp);
        sendEmail(to, subject, htmlContent);
    }

    /**
     * Sends an HTML email with pre-rendered content.
     */
    public void sendEmail(String to, String subject, String htmlContent) throws MessagingException {
        MimeMessage message = javaMailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(htmlContent, true);

        javaMailSender.send(message);
    }
}
