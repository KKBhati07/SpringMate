package com.example.SpringMate.User.DTO;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class UserDTO {
    @NotNull
    private String name;

    @Email
    @NotNull
    private String email;

    @Length(min = 5)
    @NotNull
    private String password;

    private String role;
}
