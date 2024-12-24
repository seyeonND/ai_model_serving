package com.sy.test.sytest.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Getter;

@Entity
@Getter
public class TestTable {
    @Id
    private String testId;

    private String testName;
}
