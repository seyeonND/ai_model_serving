package com.jpa.spring.jpaspring.vo;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

public class WorkspaceMemberVo {

    @Builder
    @Getter
    @Setter
    public static class WorkspaceMemberGetVo{
        private String memberId;
        private String roleType;
    }
}
