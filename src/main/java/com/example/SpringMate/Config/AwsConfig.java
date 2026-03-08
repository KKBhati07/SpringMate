package com.example.SpringMate.Config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.presigner.S3Presigner;
import software.amazon.awssdk.services.ses.SesClient;

@Configuration
@RequiredArgsConstructor
public class AwsConfig {

    private final AppProperties appProperties;

    @Bean
    @Lazy
    public S3Client amazonS3Client() {
        return S3Client.builder()
                .region(Region.of(appProperties.getAws().getRegion()))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    @Bean
    @Lazy
    public S3Presigner s3Presigner() {
        return S3Presigner.builder()
                .region(Region.of(appProperties.getAws().getRegion()))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

    @Bean
    @Lazy
    public SesClient sesClient() {
        return SesClient.builder()
                .region(Region.of(appProperties.getAws().getRegion()))
                .credentialsProvider(DefaultCredentialsProvider.create())
                .build();
    }

}