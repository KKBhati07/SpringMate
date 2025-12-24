package com.example.SpringMate.Auth.Service;

import com.example.SpringMate.Shared.Service.EmailService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class OtpNotificationDispatcher {
    private final EmailService emailService;

    @Async("appDefault")
    @Retry(name = "sendEmail")
    @CircuitBreaker(name = "sendEmail", fallbackMethod = "handleEmailFailure")
    public void dispatchEmail(String to, String subject, String otp) throws MessagingException {
        log.info("OTP email dispatch attempt subject={}", subject);
        emailService.sendEmail(to, subject, otp);
        log.info("OTP email dispatched successfully subject={}", subject);
    }

    public String handleEmailFailure(Exception ex) {
        log.warn("OTP email dispatch failed after retries, triggering fallback", ex);
        return null;

    }


}
