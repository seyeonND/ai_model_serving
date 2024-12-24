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
public class WorkspaceMemberId implements Serializable{
    private String workspaceId;
    private String memberId;

    protected WorkspaceMemberId(){

    }
}
