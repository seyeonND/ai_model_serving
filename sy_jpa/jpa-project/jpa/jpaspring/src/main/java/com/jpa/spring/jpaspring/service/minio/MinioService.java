package com.jpa.spring.jpaspring.service.minio;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class MinioService {
    // private final MinioClient minioClient;

    // @Autowired
    // public MinioService(@Value("${spring.minio.url}") String url,
    //         @Value("${spring.minio.access-key}") String accessKey,
    //         @Value("${spring.minio.secret-key}") String secretKey) {
    //     this.minioClient = MinioClient.builder()
    //             .endpoint(url)
    //             .credentials(accessKey, secretKey)
    //             .build();
    // }

    // public void createBucket(String bucketName) {
    //     try {
    //         // 버킷 존재 확인 후 생성
    //         if (!minioClient.bucketExists(BucketExistsArgs.builder().bucket(bucketName).build())) {
    //             // 버킷 생성
    //             minioClient.makeBucket(MakeBucketArgs.builder().bucket(bucketName).build());
    //             log.info("Bucket created: " + bucketName);
    //         } else {
    //             log.info("Bucket already exists: " + bucketName);
    //         }
    //     } catch (MinioException | IOException | InvalidKeyException | NoSuchAlgorithmException e) {
    //         e.printStackTrace();
    //         throw new RuntimeException("Error creating bucket: " + bucketName, e);
    //     }
    // }
}
