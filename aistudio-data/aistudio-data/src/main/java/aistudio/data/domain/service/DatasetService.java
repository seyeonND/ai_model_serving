package aistudio.data.domain.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import aistudio.data.domain.dto.data.DataFileIdRequestDTO;
import aistudio.data.domain.dto.data.DataTagRequestDTO;
import aistudio.data.domain.dto.dataset.DatasetFilterDTO;
import aistudio.data.domain.dto.dataset.DatasetGetDTO;
import aistudio.data.domain.dto.dataset.DatasetIdRequestDTO;
import aistudio.data.domain.dto.dataset.DatasetModifyDTO;
import aistudio.data.domain.dto.dataset.DatasetSaveDTO;
import aistudio.data.domain.model.DatasetEntity;
import aistudio.data.domain.model.DatasetFileEntity;
import aistudio.data.domain.model.DatasetFileId;
import aistudio.data.domain.model.FileMetadata;
import aistudio.data.domain.repository.DatasetFileRepository;
import aistudio.data.domain.repository.DatasetRepository;
import aistudio.data.domain.repository.FileMetadataRepository;
import aistudio.data.domain.service.common.IdGenerateService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class DatasetService {

    private final DatasetRepository datasetRepository;
    private final DatasetFileRepository datasetFileRepository;
    private final FileMetadataRepository fileMetadataRepository;
    private final IdGenerateService idGenerateService;

    @Transactional
    public DatasetEntity insertDataset(DatasetSaveDTO datasetSaveDTO) {
        // 데이터셋 ID 생성
        String datasetId = idGenerateService.generateId();

        // 태그 변환
        List<String> tagStrings = new ArrayList<>();
        if (!datasetSaveDTO.getTags().isEmpty() && datasetSaveDTO.getTags() != null) {
            tagStrings = datasetSaveDTO.getTags().stream()
                    .map(DataTagRequestDTO::toString)
                    .collect(Collectors.toList());
        }

        // 데이터셋 엔티티 생성
        DatasetEntity datasetEntity = DatasetEntity.builder()
                .workspaceId(datasetSaveDTO.getWorkspaceId())
                .datasetId(datasetId)
                .datasetName(datasetSaveDTO.getDatasetName())
                .tags(tagStrings)
                .registerId("admin")
                .build();

        // 데이터셋 저장
        datasetRepository.save(datasetEntity);

        // 파일 ID 리스트 변환
        if (!datasetSaveDTO.getFileIds().isEmpty() && datasetSaveDTO.getFileIds() != null) {
            List<String> fileIdList = datasetSaveDTO.getFileIds().stream()
                    .map(DataFileIdRequestDTO::getFileId)
                    .collect(Collectors.toList());

            // 파일 메타데이터 조회
            List<FileMetadata> files = fileMetadataRepository.findAllById(fileIdList);
            if (files.isEmpty()) {
                throw new IllegalArgumentException("No valid file metadata found for the provided file IDs.");
            }

            // 파일 메타데이터를 데이터셋에 매핑하여 저장
            fileIdList.forEach(fileId -> {
                DatasetFileId datasetFileId = DatasetFileId.builder()
                        .datasetId(datasetId)
                        .fileId(fileId)
                        .build();

                DatasetFileEntity datasetFileEntity = DatasetFileEntity.builder()
                        .datasetFileId(datasetFileId)
                        .build();

                datasetFileRepository.save(datasetFileEntity);
            });
        }

        return datasetEntity;
    }

    public List<DatasetEntity> getDatasetList(DatasetFilterDTO datasetFilterDTO) {
        List<DatasetEntity> datasetList = new ArrayList<>();
        // 1. 내가 만든 데이터셋 & workspaceId & tag
        if (datasetFilterDTO.getWorkspaceIds() != null
                && !datasetFilterDTO.getWorkspaceIds().isEmpty()
                && datasetFilterDTO.getMyDataset()
                && datasetFilterDTO.getTags() != null
                && !datasetFilterDTO.getTags().isEmpty()) {
            datasetList = datasetRepository.findByWorkspaceIdsAndRegisterIdAndTags(
                    datasetFilterDTO.getWorkspaceIds(),
                    "admin",
                    datasetFilterDTO.getTags().toArray(new String[0]));
        }
        // 2. workspaceId & tag
        else if (datasetFilterDTO.getWorkspaceIds() != null
                && !datasetFilterDTO.getWorkspaceIds().isEmpty()
                && datasetFilterDTO.getTags() != null
                && !datasetFilterDTO.getTags().isEmpty()) {
            datasetList = datasetRepository.findByWorkspaceIdsAndTags(
                    datasetFilterDTO.getWorkspaceIds(),
                    datasetFilterDTO.getTags().toArray(new String[0]));
        }
        // 3. 내가 만든 데이터셋 & tag
        else if (datasetFilterDTO.getMyDataset()
                && datasetFilterDTO.getTags() != null
                && !datasetFilterDTO.getTags().isEmpty()) {
            datasetList = datasetRepository.findByRegisterIdAndTags(
                    "admin",
                    datasetFilterDTO.getTags().toArray(new String[0]));
        }
        // 4. 태그만 검색
        else if (datasetFilterDTO.getTags() != null
                && !datasetFilterDTO.getTags().isEmpty()) {
            datasetList = datasetRepository.findByTags(datasetFilterDTO.getTags().toArray(new String[0]));
        }
        // 5. 기존 조건
        else if (datasetFilterDTO.getWorkspaceIds() != null
                && !datasetFilterDTO.getWorkspaceIds().isEmpty()
                && datasetFilterDTO.getMyDataset()) {
            datasetList = datasetRepository.findByWorkspaceIdsAndRegisterId(
                    datasetFilterDTO.getWorkspaceIds(),
                    "admin");
        } else if (datasetFilterDTO.getWorkspaceIds() != null
                && !datasetFilterDTO.getWorkspaceIds().isEmpty()) {
            datasetList = datasetRepository.findByWorkspaceIds(datasetFilterDTO.getWorkspaceIds());
        } else if (datasetFilterDTO.getMyDataset()) {
            datasetList = datasetRepository.findByRegisterId("admin");
        } else {
            datasetList = datasetRepository.findAll();
        }

        return datasetList;
    }

    @Transactional
    public DatasetGetDTO getDataset(DatasetIdRequestDTO datasetIdRequestDTO) {
        // 데이터셋 조회
        DatasetEntity datasetEntity = datasetRepository.findById(datasetIdRequestDTO.getDatasetId())
                .orElseThrow(() -> new IllegalArgumentException("Dataset not found"));

        // 파일 ID 리스트 조회
        List<DatasetFileEntity> fileEntities = datasetFileRepository
                .findByDatasetFileIdDatasetId(datasetIdRequestDTO.getDatasetId());

        // 파일 ID 리스트를 DataFileIdRequestDTO 리스트로 변환
        List<DataFileIdRequestDTO> fileIds = fileEntities.stream()
                .map(fileEntity -> new DataFileIdRequestDTO(fileEntity.getDatasetFileId().getFileId()))
                .collect(Collectors.toList());

        // 태그 리스트를 DataTagRequestDTO 리스트로 변환
        List<DataTagRequestDTO> tags = datasetEntity.getTags().stream()
                .map(tag -> new DataTagRequestDTO(tag))
                .collect(Collectors.toList());

        // DatasetGetDTO 객체 반환
        return DatasetGetDTO.builder()
                .datasetId(datasetEntity.getDatasetId())
                .workspaceId(datasetEntity.getWorkspaceId()) // workspaceId가 DatasetEntity에 포함되어 있다고 가정
                .datasetName(datasetEntity.getDatasetName())
                .fileIds(fileIds)
                .tags(tags)
                .registerId(datasetEntity.getRegisterId())
                .registerDatetime(datasetEntity.getRegisterDatetime())
                .modifyId(datasetEntity.getModifyId())
                .modifyDatetime(datasetEntity.getModifyDatetime())
                .build();
    }

    @Transactional
    public DatasetEntity updateDataset(DatasetModifyDTO datasetModifyDTO) {
        // 기존 데이터셋 조회
        Optional<DatasetEntity> existingDataset = datasetRepository.findById(datasetModifyDTO.getDatasetId());
        if (existingDataset.isPresent()) {
            DatasetEntity datasetEntity = existingDataset.get();
            datasetEntity.setModifyId("admin");

            // 수정할 데이터 업데이트
            if (datasetModifyDTO.getDatasetName() != null && !"".equals(datasetModifyDTO.getDatasetName())) {
                datasetEntity.setDatasetName(datasetModifyDTO.getDatasetName());
            }

            // 기존 datasetId에 해당하는 DatasetFileEntity 삭제
            datasetFileRepository.deleteByDatasetId(datasetModifyDTO.getDatasetId());

            if (!datasetModifyDTO.getFileIds().isEmpty() && datasetModifyDTO.getFileIds() != null) {
                List<String> fileIdList = datasetModifyDTO.getFileIds().stream()
                        .map(DataFileIdRequestDTO::getFileId)
                        .collect(Collectors.toList());

                // 파일 메타데이터를 데이터셋에 매핑하여 새로 추가
                fileIdList.forEach(fileId -> {
                    DatasetFileId datasetFileId = DatasetFileId.builder()
                            .datasetId(datasetModifyDTO.getDatasetId())
                            .fileId(fileId)
                            .build();

                    DatasetFileEntity datasetFileEntity = DatasetFileEntity.builder()
                            .datasetFileId(datasetFileId)
                            .build();

                    datasetFileRepository.save(datasetFileEntity);
                });
            }

            // 태그 수정
            List<String> tagStrings = new ArrayList<>();
            if (!datasetModifyDTO.getTags().isEmpty() && datasetModifyDTO.getTags() != null) {
                tagStrings = datasetModifyDTO.getTags().stream()
                        .map(DataTagRequestDTO::toString)
                        .collect(Collectors.toList());

            }
            datasetEntity.setTags(tagStrings);

            return datasetRepository.save(datasetEntity);
        } else {
            throw new IllegalArgumentException("Dataset not found for update");
        }
    }

    @Transactional
    public boolean deleteDataset(DatasetIdRequestDTO datasetIdRequestDTO) {
        Optional<DatasetEntity> datasetEntity = datasetRepository.findById(datasetIdRequestDTO.getDatasetId());
        if (datasetEntity.isPresent()) {
            try {
                // 데이터셋에 관련된 파일 삭제
                datasetFileRepository.deleteByDatasetId(datasetIdRequestDTO.getDatasetId());

                // 데이터셋 삭제
                datasetRepository.deleteById(datasetIdRequestDTO.getDatasetId());

                return true;
            } catch (Exception e) {
                // 예외 처리 및 로그 남기기 (필요에 따라)
                // 예외가 발생하면 트랜잭션이 자동으로 롤백됨
                log.error("Error deleting dataset: {}", datasetIdRequestDTO.getDatasetId(), e);
                return false;
            }
        }
        return false;
    }

    @Transactional
    public boolean deleteDatasetByFileId(String fileId) {
        List<DatasetFileEntity> datasetFileEntity = datasetFileRepository.findByDatasetFileIdFileId(fileId);
        if (datasetFileEntity != null) {
            try {
                datasetFileRepository.deleteByFileId(fileId);
                return true;
            } catch (Exception e) {
                log.error("Error deleting dataset: {}", fileId, e);
                return false;
            }
        }
        return false;
    }

}
