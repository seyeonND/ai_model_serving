package com.jpa.spring.jpaspring.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.jpa.spring.jpaspring.entity.Workspace;

@Repository
public interface WorkspaceRepository extends JpaRepository<Workspace, String>{
}
