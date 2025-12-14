package com.example.SpringMate.Storage.Service;

import com.example.SpringMate.Shared.Constants;
import com.example.SpringMate.Shared.Enum.AwsS3Directory;
import com.example.SpringMate.Shared.Exception.BadRequestException;
import com.example.SpringMate.Storage.DTO.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class StorageService {

    private final AwsS3Service awsS3Service;

    public PresignBatchResponseDto getPresignPutUrls(PresignBatchRequestDto batchRequest) {

        if (batchRequest == null || batchRequest.getFiles() == null || batchRequest.getFiles().isEmpty()) {
            throw new BadRequestException("Request must contain at least one file to presign.");
        }

        List<PresignResponseDto> successes = new ArrayList<>();
        List<PresignFailureDto> failures = new ArrayList<>();

        for (PresignRequestDto fileReq : batchRequest.getFiles()) {
            AwsS3Directory directory = fileReq.getDirectory() != null
                    ? fileReq.getDirectory()
                    : batchRequest.getDirectory();

            try {
                successes.add(getPresignPutUrl(fileReq, directory));

            } catch (Exception e) {
                e.printStackTrace();

                String reason;
                if (e instanceof BadRequestException) {
                    reason = e.getMessage();
                } else {
                    reason = "Failed to generate presigned URL";
                }

                failures.add(new PresignFailureDto(fileReq.getFileName(), reason));
            }
        }

        return PresignBatchResponseDto.builder()
                .presigns(successes)
                .failures(failures)
                .build();
    }


    public PresignResponseDto getPresignPutUrl(PresignRequestDto presignRequestDto, AwsS3Directory directory) {
        if (presignRequestDto == null) {
            throw new BadRequestException("Invalid body!");
        }

        if (directory == null || directory.getName() == null || directory.getName().isBlank()) {
            throw new BadRequestException("Missing directory for file: " + presignRequestDto.getFileName());
        }

        if (presignRequestDto.getFileName() == null || presignRequestDto.getFileName().isBlank()) {
            throw new BadRequestException("fileName is required for each file.");
        }

        if (presignRequestDto.getContentType() == null || presignRequestDto.getContentType().isBlank()) {
            throw new BadRequestException("contentType is required for file: " + presignRequestDto.getFileName());
        }

        PresignResult res = awsS3Service.generatePresignedPutUrl(
                directory,
                Constants.AWS.BUCKET_NAME,
                presignRequestDto.getFileName(),
                presignRequestDto.getContentType(),
                Constants.AWS.PUT_SIGNED_URI_EXPIRATION
        );

        if (res == null) {
            throw new IllegalStateException("PresignResult is null for file: " + presignRequestDto.getFileName());
        }
        if (res.url() == null || res.url().isBlank()) {
            throw new IllegalStateException("Presigned URL is empty for file: " + presignRequestDto.getFileName());
        }

        Map<String, String> headers = new HashMap<>();
        if (res.contentType() != null) {
            headers.put("Content-Type", res.contentType());
        }

        return PresignResponseDto.builder()
                .isCover(presignRequestDto.getIsCover())
                .url(res.url())
                .objectKey(res.objectKey())
                .expiresAt(res.expiresAt())
                .headers(headers)
                .build();
    }


    public void deleteImage(String bucketName, String objectKey) {
        awsS3Service.deleteImage(bucketName, objectKey);
    }

    public String uploadImage(String bucketName, AwsS3Directory directoryName, MultipartFile imageFile) {
        return awsS3Service.uploadImage(bucketName, directoryName, imageFile);
    }

    public String getPreSignedUrl(String bucketName, String objectKey, long expiryMinutes) {
        return awsS3Service.getPreSignedUrl(bucketName, objectKey, expiryMinutes);
    }

    public boolean doesObjectExists(String bucketName, String objectKey) {
        return awsS3Service.doesObjectExist(bucketName, objectKey);
    }
}
