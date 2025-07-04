package com.example.SpringMate.Auth.DTO;

import com.example.SpringMate.Shared.Enum.OTPType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class OtpLoginRequestDto {

    @NotBlank
    private String email;

    @NotNull
    private OTPType type;

    @NotBlank
    private String otp;

}
