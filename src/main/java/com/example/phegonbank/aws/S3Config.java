package com.example.phegonbank.aws;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

@Configuration

public class S3Config {

    @Value("${aws.accessKeyId}")
    private String accessKey;

    @Value("${aws.secretAccessKey}")
    private String secretKey;

    @Value("${aws.region}")
    private String region;

    //@Value("${aws.bucketName}")
    //private String bucketName;

    @Bean
    public S3Client s3Client() {
        return new S3Client.Builder()
                    .s3Client.region(Region.of(region))
                    .credentialsProvider(StaticCredentialsProvider
                            .create(AwsBasicCredentials.create(accessKey, secretKey)))
                    .build();
    }


}
