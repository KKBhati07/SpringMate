package com.example.SpringMate.DTO;

import jakarta.validation.constraints.Email;
import lombok.Data;
import org.hibernate.validator.constraints.UUID;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UpdateUserDTO {
    @UUID
    private String uuid;
    private String name;
    @Email
    private String email;
    private String contactNo;
    private MultipartFile profileImage;
}
