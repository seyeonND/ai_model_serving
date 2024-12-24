package com.jpa.spring.jpaspring.entityId;

import java.io.Serializable;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@Builder
@AllArgsConstructor
public class ProjectMemberId implements Serializable {

    private String memberId;
    private String workspaceId;
    private String projectId;

    protected ProjectMemberId(){
        
    }
    // equals()와 hashCode() 메소드 오버라이드
}
