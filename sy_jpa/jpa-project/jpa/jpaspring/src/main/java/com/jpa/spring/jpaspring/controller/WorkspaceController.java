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
import org.springframework.web.bind.annotation.RestController;

import com.jpa.spring.jpaspring.service.WorkspaceService;
import com.jpa.spring.jpaspring.service.kafka.KafkaProducerService;
import com.jpa.spring.jpaspring.vo.WorkspaceVo.WorkspaceGetVo;
import com.jpa.spring.jpaspring.vo.WorkspaceVo.WorkspaceSaveVo;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;



@RestController
@RequestMapping("/api/workspaces")
@RequiredArgsConstructor
@Slf4j
public class WorkspaceController {
    private final WorkspaceService workspaceService;

    private final KafkaProducerService kafkaProducerService;

    @PostMapping
    public ResponseEntity<WorkspaceGetVo> createWorkspaceWithMembers(@RequestBody WorkspaceSaveVo workspaceSaveVo) {
        WorkspaceGetVo createdWorkspace = workspaceService.addWorkspaceWithMembers(workspaceSaveVo);
        kafkaProducerService.sendCreateBucketMessage(createdWorkspace.getWorkspaceId());
        log.info("MINIO bucket create Message SEND : " + createdWorkspace.getWorkspaceId());
        return new ResponseEntity<>(createdWorkspace, HttpStatus.CREATED);
    }

    @GetMapping("/{workspaceId}")
    public ResponseEntity<WorkspaceGetVo> getWorkspace(@PathVariable String workspaceId) {
        WorkspaceGetVo workspace = workspaceService.getWorkspace(workspaceId);
        if (workspace != null){
            return new ResponseEntity<>(workspace, HttpStatus.OK);
        }

        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @GetMapping
    public ResponseEntity<List<WorkspaceGetVo>> getListWorkspace() {
        List<WorkspaceGetVo> workspaces = workspaceService.getListWorkspace();
        return new ResponseEntity<>(workspaces, HttpStatus.OK);
    }

    @PatchMapping("/{workspaceId}")
    public ResponseEntity<WorkspaceGetVo> updateWorkspace(
        @PathVariable String workspaceId, @RequestBody WorkspaceSaveVo workspaceSaveVo){
        WorkspaceGetVo updatedWorkspace = workspaceService.updateWorkspace(workspaceId, workspaceSaveVo);
        if (updatedWorkspace != null){
            return new ResponseEntity<>(updatedWorkspace, HttpStatus.OK);
        }
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @DeleteMapping("/{workspaceId}")
    public ResponseEntity<Void> deleteWorkspace(@PathVariable String workspaceId){
        workspaceService.deleteWorkspace(workspaceId);
        return new ResponseEntity<>(HttpStatus.NO_CONTENT);
    }
    
    
    
}
