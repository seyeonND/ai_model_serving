package com.jpa.spring.jpaspring.service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.jpa.spring.jpaspring.entity.Workspace;
import com.jpa.spring.jpaspring.entity.WorkspaceMember;
import com.jpa.spring.jpaspring.entityId.WorkspaceMemberId;
import com.jpa.spring.jpaspring.repository.WorkspaceMemberRepository;
import com.jpa.spring.jpaspring.repository.WorkspaceRepository;
import com.jpa.spring.jpaspring.vo.WorkspaceMemberVo.WorkspaceMemberGetVo;
import com.jpa.spring.jpaspring.vo.WorkspaceVo.WorkspaceGetVo;
import com.jpa.spring.jpaspring.vo.WorkspaceVo.WorkspaceSaveVo;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class WorkspaceService {
        private final WorkspaceRepository workspaceRepository;
        private final WorkspaceMemberRepository workspaceMemberRepository;

        private final WorkspaceIdGenerateService workspaceIdGenerateService;

        @Transactional
        public WorkspaceGetVo addWorkspaceWithMembers(WorkspaceSaveVo workspaceSaveVo) {
                // 워크스페이스 저장
                Workspace workspace = Workspace.builder()
                                .workspaceId(workspaceIdGenerateService.generateBucketName())
                                .workspaceName(workspaceSaveVo.getWorkspaceName())
                                .workspaceDesc(workspaceSaveVo.getWorkspaceDesc())
                                .registerId("psy")
                                .registerDateTime(LocalDateTime.now())
                                .build();

                Workspace savedWorkspace = workspaceRepository.save(workspace);

                // 워크스페이스에 멤버들 추가
                for (String memberId : workspaceSaveVo.getMemberIds()) {
                        WorkspaceMemberId workspaceMemberId = WorkspaceMemberId.builder()
                                        .memberId(memberId)
                                        .workspaceId(savedWorkspace.getWorkspaceId())
                                        .build();

                        WorkspaceMember workspaceMember = WorkspaceMember.builder()
                                        .workspaceMemberId(workspaceMemberId)
                                        .roleType("DEFAULT_ROLE")
                                        .registerDateTime(LocalDateTime.now())
                                        .registerId("psy") // 등록자 ID
                                        .build();

                        workspaceMemberRepository.save(workspaceMember);
                }

                return converToWorkspaceGetVo(savedWorkspace);
        }

        // 워크스페이스 조회
        public WorkspaceGetVo getWorkspace(String workspaceId) {
                Optional<Workspace> workspace = workspaceRepository.findById(workspaceId);
                return workspace.map(this::converToWorkspaceGetVo).orElse(null);
        }

        // 모든 워크스페이스 조회
        public List<WorkspaceGetVo> getListWorkspace() {
                List<Workspace> workspaces = workspaceRepository.findAll();
                return workspaces.stream().map(this::converToWorkspaceGetVo).collect(Collectors.toList());
        }

        // 워크스페이스 수정
        @Transactional
        public WorkspaceGetVo updateWorkspace(String workspaceId, WorkspaceSaveVo workspaceSaveVo) {
                Optional<Workspace> existingWorkspaceOpt = workspaceRepository.findById(workspaceId);
                if (existingWorkspaceOpt.isPresent()) {
                        Workspace existingWorkspace = existingWorkspaceOpt.get();
                        // WorkspaceMember 삭제 및 추가 로직 수정
                        List<String> memberIds = Optional.ofNullable(workspaceSaveVo.getMemberIds())
                                        .orElse(Collections.emptyList());

                        // 기존에 저장된 WorkspaceMember 가져오기
                        List<WorkspaceMember> existingMembers = workspaceMemberRepository
                                        .findByWorkspace_WorkspaceId(workspaceId)
                                        .orElseThrow();

                        // 새로운 멤버 ID가 비어 있다면 기존 멤버 유지
                        if (memberIds.isEmpty()) {
                                memberIds = existingMembers.stream()
                                                .map(member -> member.getWorkspaceMemberId().getMemberId())
                                                .collect(Collectors.toList());
                        } else {
                                // 새로운 멤버 ID가 있다면 기존 멤버 삭제 후 업데이트
                                workspaceMemberRepository.deleteAllByWorkspace_WorkspaceId(workspaceId);
                        }

                        // 멤버 저장
                        memberIds.forEach(memberId -> {
                                WorkspaceMember workspaceMember = WorkspaceMember.builder()
                                                .workspaceMemberId(
                                                                WorkspaceMemberId.builder()
                                                                                .memberId(memberId)
                                                                                .workspaceId(existingWorkspace
                                                                                                .getWorkspaceId())
                                                                                .build())
                                                .roleType("DEFAULT_ROLE")
                                                .registerDateTime(LocalDateTime.now())
                                                .registerId("psy") // 등록자 ID
                                                .build();

                                workspaceMemberRepository.save(workspaceMember);
                        });

                        // Workspace 업데이트
                        Workspace updatedWorkspace = existingWorkspace.toBuilder()
                                        .workspaceName(Optional.ofNullable(workspaceSaveVo.getWorkspaceName())
                                                        .orElse(existingWorkspace.getWorkspaceName()))
                                        .workspaceDesc(Optional.ofNullable(workspaceSaveVo.getWorkspaceDesc())
                                                        .orElse(existingWorkspace.getWorkspaceDesc()))
                                        .modifyDateTime(LocalDateTime.now())
                                        .modifyId("psy")
                                        .build();

                        workspaceRepository.save(updatedWorkspace);

                        return converToWorkspaceGetVo(updatedWorkspace);

                }
                return null;
        }

        @Transactional
        public void deleteWorkspace(String workspaceId) {
                workspaceMemberRepository.deleteAllByWorkspace_WorkspaceId(workspaceId);
                workspaceRepository.deleteById(workspaceId);
        }

        private WorkspaceGetVo converToWorkspaceGetVo(Workspace workspace) {
                List<WorkspaceMemberGetVo> memberVos = workspaceMemberRepository
                                .findByWorkspace_WorkspaceId(workspace.getWorkspaceId())
                                .orElseThrow(() -> new RuntimeException("No Workspace Members"))
                                .stream()
                                .map(member -> WorkspaceMemberGetVo.builder()
                                                .memberId(member.getWorkspaceMemberId().getMemberId())
                                                .roleType(member.getRoleType())
                                                .build())
                                .collect(Collectors.toList());

                return WorkspaceGetVo.builder()
                                .workspaceId(workspace.getWorkspaceId())
                                .workspaceName(workspace.getWorkspaceName())
                                .workspaceDesc(workspace.getWorkspaceDesc())
                                .workspaceMembers(memberVos)
                                .build();

        }
}
