package com.jpa.spring.jpaspring.vo;

import java.util.List;

import com.jpa.spring.jpaspring.vo.ProjectMemberVo.ProjectMemberGetVo;

import lombok.Builder;
import lombok.Getter;

public class ProjectVo {

    @Builder
    @Getter
    public static class ProjectGetVo{
        private String projectId;

        private String workspaceId;
        private String projectType;
        private String projectName;
        private String projectDesc;

        private List<ProjectMemberGetVo> projectMembers;  // 프로젝트에 속한 멤버들
    }

    @Getter
    public static class ProjectSaveVo{
        private String workspaceId;
        private String projectType;
        private String projectName;
        private String projectDesc;
        private List<String> memberIds;  // 프로젝트에 추가할 멤버들의 ID 리스트
    }
}
