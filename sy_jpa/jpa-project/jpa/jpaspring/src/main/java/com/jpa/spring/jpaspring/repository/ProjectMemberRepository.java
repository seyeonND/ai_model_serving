package com.jpa.spring.jpaspring.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jpa.spring.jpaspring.entity.ProjectMember;
import com.jpa.spring.jpaspring.entityId.ProjectMemberId;

@Repository
public interface ProjectMemberRepository extends JpaRepository<ProjectMember, ProjectMemberId> {

    Optional<List<ProjectMember>> findByProject_ProjectId(String projectId);


    void deleteAllByProject_ProjectId(String projectId); // projectId로 삭제
}
