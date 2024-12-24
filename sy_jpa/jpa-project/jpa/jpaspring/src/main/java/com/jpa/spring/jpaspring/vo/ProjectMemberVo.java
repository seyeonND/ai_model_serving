package com.jpa.spring.jpaspring.vo;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

public class ProjectMemberVo {

    @Builder
    @Getter
    @Setter
    public static class ProjectMemberGetVo{
        private String memberId;
        private String roleType;
    }
}
