package com.example.SpringMate.Auth.DTO;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class SessionResolveRequestDto {

    @NotBlank
    private String sessionId;

}
