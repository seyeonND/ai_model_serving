package com.jpa.spring.jpaspring.service;

import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.stereotype.Service;

@Service
public class WorkspaceIdGenerateService {
    // S3 규격에 맞는 버킷 이름을 확인하는 정규식
    private static final Pattern VALID_BUCKET_NAME_PATTERN = Pattern.compile("^(?![0-9]+$)([a-z0-9-]{3,63})$");

    // 파라미터 없이 유효한 버킷 이름을 생성하는 메소드
    public String generateBucketName() {
        // 기본적으로 UUID를 기반으로 유일한 이름을 생성
        String bucketName = "workspace-" + UUID.randomUUID().toString().replace("-", "").toLowerCase();

        // 버킷 이름이 규칙에 맞지 않으면 수정
        if (!isValidBucketName(bucketName)) {
            // 예시: 불법 문자를 하이픈으로 교체하거나 추가하는 로직
            bucketName = bucketName.replaceAll("[^a-z0-9-]", "-");

            // 시작과 끝에 하이픈을 제거
            bucketName = bucketName.replaceAll("^-+", "").replaceAll("-+$", "");

            // 최소 3자 이상 63자 이하로 만들기
            if (bucketName.length() < 3) {
                bucketName = bucketName + "minio"; // 3자 미만이면 "minio"를 추가
            }
            if (bucketName.length() > 63) {
                bucketName = bucketName.substring(0, 63); // 63자 초과 시 잘라냄
            }
        }

        return bucketName;
    }

    // 버킷 이름이 유효한지 확인하는 메소드
    private boolean isValidBucketName(String name) {
        return VALID_BUCKET_NAME_PATTERN.matcher(name).matches();
    }
}
