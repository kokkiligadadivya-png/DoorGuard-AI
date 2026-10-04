package com.doorguard.backend.service;

import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LearningService {

    // key = "location|visitorType", value = score adjustment (negative = trusted)
    private final Map<String, Integer> adjustments = new ConcurrentHashMap<>();

    private String key(String location, String visitorType) {
        return location + "|" + visitorType;
    }

    public int adjustmentFor(String location, String visitorType) {
        return adjustments.getOrDefault(key(location, visitorType), 0);
    }

    public void learnSafe(String location, String visitorType) {
        adjustments.merge(key(location, visitorType), -25, (old, delta) -> Math.max(-50, old + delta));
    }

    public void learnThreat(String location, String visitorType) {
        adjustments.merge(key(location, visitorType), 15, (old, delta) -> Math.min(30, old + delta));
    }

    public Map<String, Integer> snapshot() {
        return new TreeMap<>(adjustments);
    }
}