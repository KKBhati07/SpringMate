package com.example.SpringMate.Shared.Service;

import com.example.SpringMate.Config.AppProperties;
import com.example.SpringMate.Shared.Constants;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.services.ses.model.*;
import software.amazon.awssdk.services.ses.SesClient;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {
    private final EmailTemplateService emailTemplateService;
    private final SesClient sesClient;
    private final AppProperties appProperties;

    public void sendOtpEmail(String to, String subject, String otp) {
        String htmlContent = emailTemplateService.generateOTPEmail(Constants.EmailHeaders.LOGIN, otp);
        sendEmail(to, subject, htmlContent);
    }

    /**
     * Sends an HTML email using AWS SES.
     */
    public void sendEmail(String to, String subject, String htmlContent) {
        if (!appProperties.getEmail().isEnabled()) {
            log.debug("Email disabled (EMAIL_ENABLED=false); skipping send to {}", to);
            return;
        }

        SendEmailRequest request = SendEmailRequest.builder()
                .destination(Destination.builder()
                        .toAddresses(to)
                        .build())
                .message(Message.builder()
                        .subject(Content.builder()
                                .data(subject)
                                .build())
                        .body(Body.builder()
                                .html(Content.builder()
                                        .data(htmlContent)
                                        .build())
                                .build())
                        .build())
                .source(appProperties.getAws().getSesSourceEmail())
                .build();

        sesClient.sendEmail(request);
    }
}
