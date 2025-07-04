package com.example.SpringMate.User.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class CreateUserResponseDto {
    private boolean created;
    private boolean alreadyExists;

}
