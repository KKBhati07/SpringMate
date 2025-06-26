package com.example.SpringMate.Auth.DTO;

import com.example.SpringMate.Shared.Enum.OTPType;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class OTPRequestDTO {

    @NotNull
    private OTPType type;

    @Email
    @NotBlank
    private String email;
}
