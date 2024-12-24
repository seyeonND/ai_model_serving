package com.jpa.spring.jpaspring.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.jpa.spring.jpaspring.entity.Member;

public interface MemberRepository extends JpaRepository<Member, String>{    

}
