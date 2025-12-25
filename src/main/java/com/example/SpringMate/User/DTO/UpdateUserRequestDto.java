package com.example.SpringMate.User.DTO;

import jakarta.validation.constraints.Email;
import lombok.Data;

import java.util.UUID;

@Data
public class UpdateUserRequestDto {

    private UUID uuid;
    private String name;

    @Email(message = "Invalid email format")
    private String email;
    private String contactNo;
    private String profileUrl;
}
