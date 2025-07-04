package com.example.SpringMate.User.DTO;

import jakarta.validation.constraints.Email;
import lombok.Data;
import org.hibernate.validator.constraints.UUID;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UpdateUserRequestDto {
    @UUID
    private java.util.UUID uuid;
    private String name;
    @Email
    private String email;
    private String contactNo;
    private MultipartFile profileImage;
}
