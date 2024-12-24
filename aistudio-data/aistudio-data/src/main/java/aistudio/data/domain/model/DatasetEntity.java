package aistudio.data.domain.model;

import java.time.LocalDateTime;
import java.util.List;

import org.hibernate.annotations.Type;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import io.hypersistence.utils.hibernate.type.array.ListArrayType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "dataset")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Builder
public class DatasetEntity {

    @Id
    @Column(name = "dataset_id", columnDefinition = "varchar(12)", nullable = false)
    private String datasetId;

    @Column(name = "workspace_id", columnDefinition = "varchar(12)", nullable = false)
    private String workspaceId;

    @Column(name = "dataset_name", columnDefinition = "varchar(200)", nullable = false)
    private String datasetName;

    @Type(ListArrayType.class)
    @Column(name = "tags", columnDefinition = "text[]")
    private List<String> tags;

    @Column(name = "register_id", columnDefinition = "varchar(100)", nullable = false, updatable = false)
    private String registerId;

    @CreatedDate
    @Column(name = "register_datetime", nullable = false, updatable = false)
    private LocalDateTime registerDatetime;

    @Column(name = "modify_id", columnDefinition = "varchar(100)", nullable = true, updatable = true)
    private String modifyId;

    @LastModifiedDate
    @Column(name = "modify_datetime", nullable = true, updatable = true)
    private LocalDateTime modifyDatetime;
}
