package com.example.SpringMate.Service;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.example.SpringMate.Util.AwsS3Directory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.Objects;
import java.util.UUID;

@Service
public class AwsS3Service {
    private final AmazonS3 s3Client;

    @Autowired
    public AwsS3Service(AmazonS3 s3Client) {
        this.s3Client = s3Client;

    }

    public String uploadImage(String bucketName, AwsS3Directory directoryName, MultipartFile imageFile) {
        try {
            String originalFilename = imageFile.getOriginalFilename();
            String fileExtension = "";
            if (originalFilename != null && originalFilename.contains(".")) {
                fileExtension = originalFilename.substring(originalFilename.lastIndexOf("."));
            }
            String uniqueFileName = UUID.randomUUID() + fileExtension;
            String objectKey = directoryName.getName() + "/" + uniqueFileName;
            File file = convertMultipartFileToFile(imageFile);
            PutObjectRequest putRequest = new PutObjectRequest(bucketName, objectKey, file);
            s3Client.putObject(putRequest);
            file.delete();
            return objectKey;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getPreSignedUrl(String bucketName, String objectKey, long expirationInput) {
        try {
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

    public boolean deleteImage(String bucketName, String objectKey) {
        try {
            s3Client.deleteObject(bucketName, objectKey);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

}
