package com.jpa.spring.jpaspring.entity;

import java.time.LocalDateTime;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Entity
@Getter
@Builder(toBuilder = true)
@AllArgsConstructor
public class Workspace {
    @Id
    private String workspaceId;
    private String workspaceName;
    private String workspaceDesc;

    // @CreatedDate
    private LocalDateTime registerDateTime;
    // @CreatedBy
    private String registerId;

    // @LastModifiedDate
    private LocalDateTime modifyDateTime;
    // @LastModifiedBy
    private String modifyId;

    protected Workspace(){}
}


