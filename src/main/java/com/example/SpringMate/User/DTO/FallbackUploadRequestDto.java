package com.example.SpringMate.User.DTO;

import lombok.Data;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@Data
public class FallbackUploadRequestDto {
    private UUID uuid;
    private MultipartFile file;
}
