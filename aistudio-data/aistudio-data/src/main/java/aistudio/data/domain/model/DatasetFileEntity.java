package aistudio.data.domain.model;

import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "dataset_file")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class DatasetFileEntity {

    @EmbeddedId
    private DatasetFileId datasetFileId;
}
