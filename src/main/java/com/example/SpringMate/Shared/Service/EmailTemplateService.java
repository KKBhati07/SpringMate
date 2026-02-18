package com.example.SpringMate.Shared.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
public class EmailTemplateService {
    private final TemplateEngine templateEngine;

    public String generateOTPEmail(String heading, String otp) {
        Context context = new Context();
        context.setVariable("heading", heading);
        context.setVariable("otp", otp);
        return templateEngine.process("otp-email", context);
    }

    public String generateContactSellerEmail(
            String heading,
            String listingTitle,
            String fromName,
            String fromEmail,
            String listingUrl,
            String messageBody
    ) {
        Context context = new Context();
        context.setVariable("heading", heading);
        context.setVariable("listingTitle", listingTitle);
        context.setVariable("fromName", fromName);
        context.setVariable("fromEmail", fromEmail);
        context.setVariable("listingUrl", listingUrl);
        context.setVariable("messageBody", messageBody == null ? "" : messageBody);

        return templateEngine.process("contact-seller-email", context);
    }

}
