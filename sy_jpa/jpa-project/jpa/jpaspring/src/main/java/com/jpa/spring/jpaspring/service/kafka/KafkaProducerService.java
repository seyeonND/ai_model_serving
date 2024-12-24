package com.jpa.spring.jpaspring.service.kafka;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KafkaProducerService {

    private final KafkaTemplate<String, String> kafkaTemplate;

    public void sendCreateBucketMessage(String bucketName) {
        kafkaTemplate.send("create-bucket-topic", bucketName);
    }
}
