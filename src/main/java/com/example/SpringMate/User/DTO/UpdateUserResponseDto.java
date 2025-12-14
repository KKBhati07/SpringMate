package com.example.SpringMate.User.DTO;

import com.example.SpringMate.Util.UserDetailsDto;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UpdateUserResponseDto {
    private boolean updated;
    private boolean self;
    private UserDetailsDto userDetails;

}
