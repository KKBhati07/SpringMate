package com.example.SpringMate.DTO;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

@Data
public class UpdateUserDTO {
    private String uuid;
    private String name;
    private String email;
    private String contactNo;
    private MultipartFile profileImage;
}
