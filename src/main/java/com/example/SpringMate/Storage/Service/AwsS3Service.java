package com.example.SpringMate.Storage.Service;

import com.example.SpringMate.Shared.Enum.AwsS3Directory;
import com.example.SpringMate.Shared.Exception.InternalServerException;
import com.example.SpringMate.Storage.DTO.PresignResult;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.*;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.s3.presigner.model.GetObjectPresignRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedGetObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PresignedPutObjectRequest;
import software.amazon.awssdk.services.s3.presigner.model.PutObjectPresignRequest;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


// Migrated to SDK v2
@Slf4j
@Service
@RequiredArgsConstructor
class AwsS3Service {
    private final S3Client s3Client;
    private final S3Presigner s3Presigner;

    public PresignResult generatePresignedPutUrl(AwsS3Directory directory,
                                                 String bucketName,
                                                 String originalFilename,
                                                 String contentType,
                                                 int expiryMinutes) {

        Date expiration = new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(expiryMinutes));

        String objectKey = generateObjectKey(directory.getName(), originalFilename);

        PutObjectRequest putRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(objectKey)
                .contentType(contentType)
                .build();

        PutObjectPresignRequest putPresignRequest = PutObjectPresignRequest.builder()
                .signatureDuration(Duration.ofMinutes(expiryMinutes))
                .putObjectRequest(putRequest)
                .build();

        PresignedPutObjectRequest presignedRequest = s3Presigner.presignPutObject(putPresignRequest);

        // Note: AmazonS3 SDK v1 doesn't let you easily sign arbitrary headers like content-type through the simple API.
        // Frontend should set `Content-Type` equal to the contentType provided here.
        return new PresignResult(
                presignedRequest.url().toString(),
                objectKey,
                Instant.now().plus(Duration.ofMinutes(expiryMinutes)),
                contentType
        );
    }

    @Retry(name = "s3upload")
    @CircuitBreaker(name = "s3Upload", fallbackMethod = "uploadFallback")
    public String uploadImage(String bucketName, AwsS3Directory directory, MultipartFile imageFile) {
        try {
            String objectKey = generateObjectKey(directory.getName(), imageFile.getOriginalFilename());

            PutObjectRequest putRequest = PutObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .contentType(imageFile.getContentType())
                    .contentLength(imageFile.getSize())
                    .build();

            s3Client.putObject(putRequest,
                    RequestBody.fromInputStream(
                            imageFile.getInputStream(),
                            imageFile.getSize()
                    ));
            return objectKey;
        } catch (IOException ex) {
            log.error(
                    "S3 upload failed bucket={} fileName={}",
                    bucketName,
                    imageFile.getOriginalFilename(),
                    ex
            );
            return null;
        }
    }


    public String getPreSignedUrl(String bucketName, String objectKey, long expiryMinutes) {
        try {
            if (objectKey == null) return null;

            GetObjectRequest getObjectRequest = GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            GetObjectPresignRequest presignRequest =
                    GetObjectPresignRequest.builder()
                            .signatureDuration(Duration.ofMinutes(expiryMinutes))
                            .getObjectRequest(getObjectRequest)
                            .build();

            PresignedGetObjectRequest presignedRequest =
                    s3Presigner.presignGetObject(presignRequest);

            return presignedRequest.url().toString();
        } catch (Exception e) {
            throw new InternalServerException("Error generating pre signed URL");
        }
    }

    private File convertMultipartFileToFile(MultipartFile multipartFile) throws IOException {
        File file = new File(Objects.requireNonNull(multipartFile.getOriginalFilename()));
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(multipartFile.getBytes());
        }
        return file;
    }

    private String generateObjectKey(String directory, String originalFilename) {

        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
            // includes the ".png", ".jpg", ".pdf"
        }

        String uniqueName = UUID.randomUUID() + extension;

        if (directory == null || directory.isBlank()) {
            return uniqueName;
        }

        return directory + "/" + uniqueName;
    }


    @Async("appDefault")
    public void deleteImage(String bucketName, String objectKey) {
        try {
            DeleteObjectRequest deleteRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();
            s3Client.deleteObject(deleteRequest);
        } catch (Exception ex) {
            log.error(
                    "Failed to delete image objectKey={}",
                    objectKey,
                    ex
            );
        }
    }

    public String uploadFallback(Exception ex) {
        log.error(
                "Image upload failed after retries (fallback triggered)",
                ex
        );
        return null;
    }

    public boolean doesObjectExist(String bucketName, String objectKey) {

        try {
            HeadObjectRequest headObjectRequest = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(objectKey)
                    .build();

            s3Client.headObject(headObjectRequest);
            return true;

        } catch (NoSuchKeyException e) {
            return false;

        } catch (S3Exception e) {
            if (e.statusCode() == 404) {
                return false;
            }
            throw e;
        }
    }


}
