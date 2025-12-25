package com.example.SpringMate.Auth.DTO;

import com.example.SpringMate.Util.UserDetailsDto;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class AuthDetailsResponseDto {
    private UserDetailsDto authDetails;
    private boolean isAuthenticated;
}
