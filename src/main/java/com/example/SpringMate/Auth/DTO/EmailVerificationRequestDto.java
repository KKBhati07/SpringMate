package com.example.SpringMate.Auth.DTO;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class EmailVerificationRequestDto {

    @Email
    @NotBlank
    private String email;

    @NotBlank
    private String otp;
}
