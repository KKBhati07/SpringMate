package com.example.SpringMate.DTO;

import com.example.SpringMate.Util.OTPType;
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
