package com.example.SpringMate.Storage.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PresignFailureDto {
    private String fileName;
    private String reason;
}