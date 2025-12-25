package com.example.SpringMate.Storage.DTO;

import com.example.SpringMate.Shared.Enum.AwsS3Directory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PresignBatchRequestDto {

    private AwsS3Directory directory;
    private List<PresignRequestDto> files;
}