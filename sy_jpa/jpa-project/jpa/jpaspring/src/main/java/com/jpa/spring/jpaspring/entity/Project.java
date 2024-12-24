package com.jpa.spring.jpaspring.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Entity
@Builder(toBuilder = true)
@Getter
@AllArgsConstructor
public class Project {
    @Id
    private String projectId;

    private String workspaceId;
    private String projectType;
    private String projectName;
    private String projectDesc;
    
    // @CreatedDate
    private LocalDateTime registerDateTime;
    // @CreatedBy
    private String registerId;

    // @LastModifiedDate
    private LocalDateTime modifyDateTime;
    // @LastModifiedBy
    private String modifyId;

    protected Project(){}

}

