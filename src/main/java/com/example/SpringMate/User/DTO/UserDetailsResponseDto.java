package com.example.SpringMate.User.DTO;

import com.example.SpringMate.Util.UserDetailsDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class UserDetailsResponseDto {

    private UserDetailsDto userDetails;
    private boolean self;

}
