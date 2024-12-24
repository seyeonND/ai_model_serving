package com.jpa.spring.jpaspring.entity;

import java.time.LocalDateTime;

import com.jpa.spring.jpaspring.entityId.ProjectMemberId;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Entity
@Getter
@Builder
@AllArgsConstructor
public class ProjectMember {

    @EmbeddedId
    private ProjectMemberId projectMemberId;
    
    private String roleType;
    // @CreatedDate
    private LocalDateTime registerDateTime;
    // @CreatedBy
    private String registerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "projectId", insertable = false, updatable = false)
    private Project project;

    protected ProjectMember() {
        
    }
}
