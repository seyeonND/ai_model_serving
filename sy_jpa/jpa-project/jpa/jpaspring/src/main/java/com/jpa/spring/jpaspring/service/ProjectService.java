package com.jpa.spring.jpaspring.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.jpa.spring.jpaspring.entity.Project;
import com.jpa.spring.jpaspring.entity.ProjectMember;
import com.jpa.spring.jpaspring.entityId.ProjectMemberId;
import com.jpa.spring.jpaspring.repository.ProjectMemberRepository;
import com.jpa.spring.jpaspring.repository.ProjectRepository;
import com.jpa.spring.jpaspring.vo.ProjectMemberVo.ProjectMemberGetVo;
import com.jpa.spring.jpaspring.vo.ProjectVo.ProjectGetVo;
import com.jpa.spring.jpaspring.vo.ProjectVo.ProjectSaveVo;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProjectService {

        private final ProjectRepository projectRepository;
        private final ProjectMemberRepository projectMemberRepository;

        @Transactional
        public ProjectGetVo addProjectWithMembers(ProjectSaveVo projectSaveVo) {
                // 프로젝트 저장
                Project project = Project.builder()
                                .projectId("P" + UUID.randomUUID().toString().substring(0, 4))
                                .workspaceId(projectSaveVo.getWorkspaceId())
                                .projectType(projectSaveVo.getProjectType())
                                .projectName(projectSaveVo.getProjectName())
                                .projectDesc(projectSaveVo.getProjectDesc())
                                .registerId("psy")
                                .registerDateTime(LocalDateTime.now())
                                .build();

                // 프로젝트 저장
                Project savedProject = projectRepository.save(project);

                // 프로젝트에 멤버들 추가
                for (String memberId : projectSaveVo.getMemberIds()) {
                        ProjectMemberId projectMemberId = ProjectMemberId.builder()
                                        .memberId(memberId)
                                        .workspaceId(savedProject.getWorkspaceId())
                                        .projectId(savedProject.getProjectId())
                                        .build();

                        ProjectMember projectMember = ProjectMember.builder()
                                        .projectMemberId(projectMemberId) // 복합키 설정
                                        .roleType("DEFAULT_ROLE")
                                        .registerDateTime(LocalDateTime.now())
                                        .registerId("psy") // 등록자 ID
                                        .build();

                        // ProjectMember 저장
                        projectMemberRepository.save(projectMember);
                }

                // 프로젝트 반환
                return convertToProjectGetVo(savedProject);
        }

        // 프로젝트 조회
        public ProjectGetVo getProject(String projectId) {
                Optional<Project> project = projectRepository.findById(projectId);
                return project.map(this::convertToProjectGetVo).orElse(null);
        }

        // 모든 프로젝트 조회
        public List<ProjectGetVo> getListProject(String workspaceId) {
                List<Project> projects = projectRepository.findByWorkspaceId(workspaceId);
                return projects.stream().map(this::convertToProjectGetVo).collect(Collectors.toList());
        }

        // 프로젝트 수정
        @Transactional
        public ProjectGetVo updateProject(String projectId, ProjectSaveVo projectSaveVo) {
                Optional<Project> existingProjectOpt = projectRepository.findById(projectId);
                if (existingProjectOpt.isPresent()) {
                        Project existingProject = existingProjectOpt.get();
                        Project updatedProject = existingProject.toBuilder()
                                        .projectId(projectId)
                                        .projectName(Optional.ofNullable(projectSaveVo.getProjectName())
                                                        .orElse(existingProject.getProjectName()))
                                        .projectDesc(Optional.ofNullable(projectSaveVo.getProjectDesc())
                                                        .orElse(existingProject.getProjectDesc()))
                                        .projectType(Optional.ofNullable(projectSaveVo.getProjectType())
                                                        .orElse(existingProject.getProjectType()))
                                        .modifyDateTime(LocalDateTime.now())
                                        .modifyId("psy")
                                        .build();
                        projectRepository.save(updatedProject);

                        // 기존 멤버 삭제 후 새 멤버 추가
                        projectMemberRepository.deleteAllByProject_ProjectId(projectId);
                        for (String memberId : projectSaveVo.getMemberIds()) {
                                ProjectMemberId projectMemberId = ProjectMemberId.builder()
                                                .memberId(memberId)
                                                .workspaceId(existingProject.getWorkspaceId())
                                                .projectId(existingProject.getProjectId())
                                                .build();

                                ProjectMember projectMember = ProjectMember.builder()
                                                .projectMemberId(projectMemberId) // 복합키 설정
                                                .roleType("DEFAULT_ROLE")
                                                .registerDateTime(LocalDateTime.now())
                                                .registerId("psy") // 등록자 ID
                                                .build();

                                projectMemberRepository.save(projectMember);
                        }

                        return convertToProjectGetVo(updatedProject);
                }
                return null;
        }

        // 프로젝트 삭제
        @Transactional
        public void deleteProject(String projectId) {
                projectMemberRepository.deleteAllByProject_ProjectId(projectId); // 관련된 멤버 삭제
                projectRepository.deleteById(projectId); // 프로젝트 삭제
        }

        private ProjectGetVo convertToProjectGetVo(Project project) {
                List<ProjectMemberGetVo> memberVos = projectMemberRepository
                                .findByProject_ProjectId(project.getProjectId())
                                .orElseThrow(() -> new RuntimeException("No Project Members"))
                                .stream()
                                .map(member -> ProjectMemberGetVo.builder() // ProjectMemberGetVo로 변환
                                                .memberId(member.getProjectMemberId().getMemberId())
                                                .roleType(member.getRoleType())
                                                .build())
                                .collect(Collectors.toList());

                return ProjectGetVo.builder()
                                .projectId(project.getProjectId())
                                .workspaceId(project.getWorkspaceId())
                                .projectType(project.getProjectType())
                                .projectName(project.getProjectName())
                                .projectDesc(project.getProjectDesc())
                                .projectMembers(memberVos)
                                .build();
        }

}
