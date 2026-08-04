package com.example.phegonbank.aws;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;

    @Value("${aws.s3.bucketName}")
    private String bucketName;


    public String uploadFile(MultipartFile file, String fileName) {
        String fileName = file.getOriginalFilename();
        String fileExtension = "";
        if (fileName != null && fileName.contains(".")) {
            fileExtension = fileName.substring(fileName.lastIndexOf("."));
        }
        String newFileName = UUID.randomUUID().toString() + fileExtension;
        String s3Key = folderName + "/" + newFileName;

        PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(s3Key)
                .contentType(file.getContentType())
                .contentLength(file.getSize())
                .build();
        
                s3Client.putObject(putObjectRequest, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
                
                return s3Client.utilities().getUrl(builder -> builder.bucket(bucketName).key(s3Key)).toString();
        }

    public boolean deleteFile(String fileurl) {
        try {
            String key = fileurl.substring(fileurl.lastIndexOf("/") + 1);
            DeleteObjectRequest deleteObjectRequest = DeleteObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build();
            s3Client.deleteObject(deleteObjectRequest);
            return true;

        } catch (S3Exception e) {
            log.error("Error deleting file from S3: {}", e.getMessage());
            return false;
        }
    }

}
