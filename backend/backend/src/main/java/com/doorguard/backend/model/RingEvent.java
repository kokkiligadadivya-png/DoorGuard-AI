package com.doorguard.backend.model;

import java.time.Instant;
import java.util.List;

public record RingEvent(
        Long id,
        String deviceName,
        String eventType,          // MOTION, DOORBELL_PRESSED, PACKAGE_DETECTED, PACKAGE_REMOVED
        String location,
        String visitorType,        // UNKNOWN_PERSON, FAMILIAR_PERSON, DELIVERY_PERSON, ANIMAL, VEHICLE
        int dwellSeconds,
        boolean triedDoor,
        String description,
        Instant timestamp,
        // ---- AI fields ----
        int riskScore,             // 0-100
        String riskLevel,          // NORMAL, UNUSUAL, CRITICAL
        List<RiskFactor> riskFactors,
        String aiAnalysis,
        String analysisSource,     // TEMPLATE or LLM
        List<String> recommendedActions,
        String status              // NEW, SAFE, THREAT_CONFIRMED
) {
    public RingEvent withScoring(int score, String level, List<RiskFactor> factors,
                                 String analysis, List<String> actions) {
        return new RingEvent(id, deviceName, eventType, location, visitorType, dwellSeconds, triedDoor,
                description, timestamp, score, level, factors, analysis, "TEMPLATE", actions, status);
    }

    public RingEvent withAnalysis(String analysis, String source) {
        return new RingEvent(id, deviceName, eventType, location, visitorType, dwellSeconds, triedDoor,
                description, timestamp, riskScore, riskLevel, riskFactors, analysis, source,
                recommendedActions, status);
    }

    public RingEvent withStatus(String newStatus) {
        return new RingEvent(id, deviceName, eventType, location, visitorType, dwellSeconds, triedDoor,
                description, timestamp, riskScore, riskLevel, riskFactors, aiAnalysis, analysisSource,
                recommendedActions, newStatus);
    }
}