package com.jpa.spring.jpaspring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jpa.spring.jpaspring.entity.Project;
import java.util.List;


@Repository
public interface ProjectRepository extends JpaRepository<Project, String>{    

    List<Project> findByWorkspaceId(String workspaceId);
}
