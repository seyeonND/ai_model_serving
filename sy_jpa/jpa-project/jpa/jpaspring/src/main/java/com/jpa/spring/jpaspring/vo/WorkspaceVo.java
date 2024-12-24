package com.jpa.spring.jpaspring.vo;

import java.util.List;

import com.jpa.spring.jpaspring.vo.WorkspaceMemberVo.WorkspaceMemberGetVo;

import lombok.Builder;
import lombok.Getter;

public class WorkspaceVo {

    @Builder
    @Getter
    public static class WorkspaceGetVo{
        private String workspaceId;

        private String workspaceName;
        private String workspaceDesc;

        private List<WorkspaceMemberGetVo> workspaceMembers;
    }

    @Getter
    public static class WorkspaceSaveVo{
        private String workspaceName;
        private String workspaceDesc;

        private List<String> memberIds;
    }
}
