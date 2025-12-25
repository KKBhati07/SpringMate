package com.example.SpringMate.Storage.DTO;

import com.example.SpringMate.Shared.Enum.AwsS3Directory;
import lombok.Data;

@Data
public class PresignRequestDto {
    private String fileName, contentType;
    private Boolean isCover;
    private AwsS3Directory directory;
}
