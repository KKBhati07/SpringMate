package com.example.SpringMate.Storage.Service;

import com.example.SpringMate.Config.AppProperties;
import com.example.SpringMate.Shared.Enum.AwsS3Directory;
import com.example.SpringMate.Shared.Exception.BadRequestException;
import com.example.SpringMate.Storage.DTO.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class StorageService {

    private final AwsS3Service awsS3Service;
    private final AppProperties appProperties;

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
                log.warn(
                        "Presign URL generation failed fileName={} reason={}",
                        fileReq.getFileName(),
                        e.getMessage()
                );

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

        AppProperties.Aws awsConfig = appProperties.getAws();
        PresignResult res = awsS3Service.generatePresignedPutUrl(
                directory,
                awsConfig.getBucketName(),
                presignRequestDto.getFileName(),
                presignRequestDto.getContentType(),
                awsConfig.getPresign().getPutExpiryMinutes()
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

    /**
     * Delete an image from S3 bucket.
     */
    public void deleteImage(String objectKey) {
        awsS3Service.deleteImage(getBucketName(), objectKey);
    }

    /**
     * Upload an image to the S3 bucket.
     */
    public String uploadImage(AwsS3Directory directoryName, MultipartFile imageFile) {
        return awsS3Service.uploadImage(getBucketName(), directoryName, imageFile);
    }

    /**
     * Get a presigned GET URL for an object using bucket and expiry.
     */
    public String getPreSignedUrl(String objectKey) {
        return awsS3Service.getPreSignedUrl(
                getBucketName(),
                objectKey,
                appProperties.getAws().getPresign().getGetExpiryMinutes()
        );
    }

    /**
     * Check if an object exists in S3 bucket.
     */
    public boolean doesObjectExist(String objectKey) {
        return awsS3Service.doesObjectExist(getBucketName(), objectKey);
    }

    @Deprecated(forRemoval = true)
    public void deleteImage(String bucketName, String objectKey) {
        awsS3Service.deleteImage(bucketName, objectKey);
    }

    @Deprecated(forRemoval = true)
    public String uploadImage(String bucketName, AwsS3Directory directoryName, MultipartFile imageFile) {
        return awsS3Service.uploadImage(bucketName, directoryName, imageFile);
    }

    @Deprecated(forRemoval = true)
    public String getPreSignedUrl(String bucketName, String objectKey, long expiryMinutes) {
        return awsS3Service.getPreSignedUrl(bucketName, objectKey, expiryMinutes);
    }

    @Deprecated(forRemoval = true)
    public boolean doesObjectExists(String bucketName, String objectKey) {
        return awsS3Service.doesObjectExist(bucketName, objectKey);
    }

    /**
     * Get the S3 bucket name.
     */
    public String getBucketName() {
        return appProperties.getAws().getBucketName();
    }

    /**
     * Get the presigned URL expiry for GET requests.
     */
    public int getGetExpiryMinutes() {
        return appProperties.getAws().getPresign().getGetExpiryMinutes();
    }
}
