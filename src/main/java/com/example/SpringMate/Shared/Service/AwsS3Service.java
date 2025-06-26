package com.example.SpringMate.Shared.Service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.example.SpringMate.Shared.Enum.AwsS3Directory;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AwsS3Service {
    private final AmazonS3 s3Client;

    @Retry(name = "s3upload")
    @CircuitBreaker(name = "s3Upload", fallbackMethod = "uploadFallback")
    public String uploadImage(String bucketName, AwsS3Directory directoryName, MultipartFile imageFile) {
        try {
            String originalFilename = imageFile.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFileName = UUID.randomUUID() + fileExtension;
            String objectKey = directoryName.getName() + "/" + uniqueFileName;

            ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(imageFile.getSize());
            metadata.setContentType(imageFile.getContentType());

            PutObjectRequest putRequest = new PutObjectRequest(
                    bucketName,
                    objectKey,
                    imageFile.getInputStream(),
                    metadata);
            s3Client.putObject(putRequest);
            return objectKey;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getPreSignedUrl(String bucketName, String objectKey, long expirationInput) {
        try {
            if(objectKey == null) return null;
            Date expiration = new Date();
            expiration.setTime(expiration.getTime() + expirationInput * 60 * 1000);
            GeneratePresignedUrlRequest req = new GeneratePresignedUrlRequest(bucketName, objectKey)
                    .withMethod(HttpMethod.GET)
                    .withExpiration(expiration);
            return s3Client.generatePresignedUrl(req).toString();
        } catch (Exception e) {
            throw new RuntimeException("Error generating pre signed URL");
        }
    }

    private File convertMultipartFileToFile(MultipartFile multipartFile) throws IOException {
        File file = new File(Objects.requireNonNull(multipartFile.getOriginalFilename()));
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(multipartFile.getBytes());
        }
        return file;
    }

    @Async("appDefault")
    public void deleteImage(String bucketName, String objectKey) {
        try {
            s3Client.deleteObject(bucketName, objectKey);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public String uploadFallback(Exception ex) {
        ex.printStackTrace();
        return null;
    }

}
