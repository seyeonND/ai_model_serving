package aistudio.data.domain.model;

import java.io.Serializable;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Embeddable
@Getter
@NoArgsConstructor
@EqualsAndHashCode
public class DatasetFileId implements Serializable {

    @Column(name = "dataset_id", columnDefinition = "varchar(100)", nullable = false)
    private String datasetId;

    @Column(name = "file_id", columnDefinition = "varchar(12)", nullable = false)
    private String fileId;


    @Builder
    public DatasetFileId(String datasetId, String fileId) {
        this.datasetId = datasetId;
        this.fileId = fileId;

    }

}
