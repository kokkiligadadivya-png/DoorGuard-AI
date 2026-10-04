package com.doorguard.backend.controller;

import com.doorguard.backend.service.LearningService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/learning")
public class LearningController {

    private final LearningService learning;

    public LearningController(LearningService learning) {
        this.learning = learning;
    }

    @GetMapping
    public Map<String, Integer> getLearnedRules() {
        return learning.snapshot();
    }
}