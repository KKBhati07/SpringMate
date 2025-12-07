package com.example.SpringMate.Auth.DTO;

import com.example.SpringMate.Util.UserDetailsDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@AllArgsConstructor
@Builder
@Data
public class OtpLoginResponseDto {
    private String authToken;
    private boolean authenticated;
    private UserDetailsDto userDetails;
}
