package com.example.SpringMate.Storage.DTO;

import lombok.Builder;
import lombok.Data;

import java.util.List;

@Data
@Builder
public class PresignBatchResponseDto {
    private List<PresignResponseDto> presigns;
    private List<PresignFailureDto> failures;
}
