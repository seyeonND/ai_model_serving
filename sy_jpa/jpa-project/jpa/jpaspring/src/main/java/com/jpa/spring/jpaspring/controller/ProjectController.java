package com.jpa.spring.jpaspring.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.jpa.spring.jpaspring.service.ProjectService;
import com.jpa.spring.jpaspring.vo.ProjectVo.ProjectGetVo;
import com.jpa.spring.jpaspring.vo.ProjectVo.ProjectSaveVo;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/api/project")
@RequiredArgsConstructor
public class ProjectController {
    private final ProjectService projectService;

    // 프로젝트 생성 (POST /api/projects)
    @PostMapping
    public ResponseEntity<ProjectGetVo> createProjectWithMembers(@RequestBody ProjectSaveVo projectSaveVo) {
        ProjectGetVo createdProject = projectService.addProjectWithMembers(projectSaveVo);
        return new ResponseEntity<>(createdProject, HttpStatus.CREATED);
    }

    // 특정 프로젝트 조회 (GET /api/projects/{projectId})
    @GetMapping("/{projectId}")
    public ResponseEntity<ProjectGetVo> getProject(@PathVariable String projectId) {
        ProjectGetVo project = projectService.getProject(projectId);
        if (project != null) {
            return new ResponseEntity<>(project, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // 모든 프로젝트 조회 (GET /api/projects)
    @GetMapping
    public ResponseEntity<List<ProjectGetVo>> getListProject(@RequestParam(required = false) String workspaceId) {
        List<ProjectGetVo> projects = projectService.getListProject(workspaceId);
        return new ResponseEntity<>(projects, HttpStatus.OK);
    }

    @PatchMapping("/{projectId}")
    public ResponseEntity<ProjectGetVo> updateProject(
            @PathVariable String projectId, @RequestBody ProjectSaveVo projectSaveVo) {
        ProjectGetVo updatedProject = projectService.updateProject(projectId, projectSaveVo);
        if (updatedProject != null) {
            return new ResponseEntity<>(updatedProject, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    // 프로젝트 삭제 (DELETE /api/projects/{projectId})
    @DeleteMapping("/{projectId}")
    public ResponseEntity<Void> deleteProject(@PathVariable String projectId) {
        projectService.deleteProject(projectId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
}
