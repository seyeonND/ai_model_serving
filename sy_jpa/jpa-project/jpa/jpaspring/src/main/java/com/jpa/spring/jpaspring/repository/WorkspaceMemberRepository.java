package com.jpa.spring.jpaspring.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jpa.spring.jpaspring.entity.WorkspaceMember;
import com.jpa.spring.jpaspring.entityId.WorkspaceMemberId;

@Repository
public interface WorkspaceMemberRepository extends JpaRepository<WorkspaceMember, WorkspaceMemberId>{

    Optional<List<WorkspaceMember>> findByWorkspace_WorkspaceId(String workspaceId);

    void deleteAllByWorkspace_WorkspaceId(String workspaceId);

}
