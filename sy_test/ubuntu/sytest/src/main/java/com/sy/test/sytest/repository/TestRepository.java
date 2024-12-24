package com.sy.test.sytest.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.sy.test.sytest.entity.TestTable;

public interface TestRepository extends JpaRepository<TestTable, String>{

}
