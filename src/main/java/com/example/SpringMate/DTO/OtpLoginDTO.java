package com.example.SpringMate.DTO;

import com.example.SpringMate.Util.OTPType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class OtpLoginDTO {

    @NotBlank
    private String email;

    @NotNull
    private OTPType type;

    @NotBlank
    private String otp;

}
