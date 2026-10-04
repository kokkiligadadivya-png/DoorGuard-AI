package com.doorguard.backend.model;

import java.time.Instant;

public record Alert(
        Long id,
        Long eventId,
        String title,
        String severity,     // UNUSUAL or CRITICAL
        String message,
        String status,       // ACTIVE or RESOLVED
        int riskScore,
        Instant timestamp
) {
}