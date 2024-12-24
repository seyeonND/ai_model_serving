package aistudio.data.domain.service.common;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.ZoneId;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import aistudio.data.domain.dto.upload.FileMetadataDTO;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.RemoveObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.errors.MinioException;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class MinioUtil {

    private final MinioClient minioClient;

    // 파일 업로드
    public String uploadFile(MultipartFile file, String objectName, String bucketName) {
        try {
            InputStream inputStream = file.getInputStream();
  
            minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .stream(inputStream, file.getSize(), -1)
                            .contentType(file.getContentType())
                            .build()
            );

            if (file.getOriginalFilename().equals(objectName)) {
                return bucketName + "/" + objectName;
            } else {
                return bucketName + objectName;
            }

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("File upload failed: " + e.getMessage());
        }

    }

    // 파일 메타정보 조회
    public FileMetadataDTO getFileMetadata(String objectName, String bucketName) {
        try {
            StatObjectResponse statObjectResponse = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(bucketName)
                            .object(objectName)
                            .build());

            LocalDateTime modifyDatetime = statObjectResponse.lastModified().toInstant()
                    .atZone(ZoneId.systemDefault())
                    .toLocalDateTime();

            return new FileMetadataDTO(
                bucketName,
                objectName,
                statObjectResponse.size(),
                statObjectResponse.contentType(),
                modifyDatetime
            );
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Get Metadata failed: " + e.getMessage());
        }
    }

    // 파일 삭제
    public boolean deleteFile(String bucketName, String objectName) {
        try {
            minioClient.removeObject(
                RemoveObjectArgs.builder()
                    .bucket(bucketName)
                    .object(objectName)
                    .build()
            );
            return true;
        } catch (MinioException | InvalidKeyException | IOException | NoSuchAlgorithmException e) {
            e.printStackTrace(); 
            return false; 
        }
    }

}
