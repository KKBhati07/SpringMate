package com.example.SpringMate.Service;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class OtpNotificationDispatcher {
    private final EmailService emailService;

    @Async("appDefault")
    @Retry(name = "sendEmail")
    @CircuitBreaker(name = "sendEmail", fallbackMethod = "handleEmailFailure")
    public void dispatchEmail(String to, String subject, String otp) throws MessagingException {
        emailService.sendEmail(to,subject,otp);
    }

    public String handleEmailFailure(Exception ex){
        ex.printStackTrace();
        //TODO : implement logging logic
        return null;

    }



}
