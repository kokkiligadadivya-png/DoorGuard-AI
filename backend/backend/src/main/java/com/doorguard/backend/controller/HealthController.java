package com.doorguard.backend.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {

    @GetMapping("/")
    public String home() {
        return "DoorGuard AI Backend is Running!";
    }

    @GetMapping("/api/health")
    public String health() {
        return "DoorGuard AI Backend is Healthy!";
    }
}