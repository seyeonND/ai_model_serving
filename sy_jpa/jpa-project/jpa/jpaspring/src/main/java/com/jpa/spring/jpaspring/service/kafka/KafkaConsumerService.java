package com.jpa.spring.jpaspring.service.kafka;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import com.jpa.spring.jpaspring.service.minio.MinioService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class KafkaConsumerService {
    // private final MinioService minioService;

    // @KafkaListener(topics = "create-bucket-topic", groupId = "my-group")
    public void consume(String bucketName) {
        
        // MinIO에서 버킷 생성
        // minioService.createBucket(bucketName);
        log.info("JPA create-bucket-topic consume ! ");
    }
}
