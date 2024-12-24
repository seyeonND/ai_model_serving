package com.sy.test.sytest.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.sy.test.sytest.entity.TestTable;
import com.sy.test.sytest.repository.TestRepository;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/test")
public class TestController {
    private final TestRepository testRepository;

    @GetMapping("/")
    public List<TestTable> getTest() {
        return testRepository.findAll();
    }    
}
