package com.example.SpringMate.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

@Service
@RequiredArgsConstructor
public class EmailTemplateService {
    private final TemplateEngine templateEngine;

    public String generateOTPEmail(String heading, String otp){
        Context context = new Context();
        context.setVariable("heading", heading);
        context.setVariable("otp",otp);
        return templateEngine.process("otp-email", context);
    }


}
