package aistudio.data.domain.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import aistudio.data.domain.model.DatasetFileEntity;
import aistudio.data.domain.model.DatasetFileId;

@Repository
public interface DatasetFileRepository extends JpaRepository<DatasetFileEntity, DatasetFileId> {

    List<DatasetFileEntity> findByDatasetFileIdDatasetId(String datasetId);

    @Modifying
    @Query("DELETE FROM DatasetFileEntity d WHERE d.datasetFileId.datasetId = :datasetId")
    void deleteByDatasetId(String datasetId);

    List<DatasetFileEntity> findByDatasetFileIdFileId(String fileId);

    @Modifying
    @Query("DELETE FROM DatasetFileEntity d WHERE d.datasetFileId.fileId = :fileId")
    void deleteByFileId(String fileId);

}
