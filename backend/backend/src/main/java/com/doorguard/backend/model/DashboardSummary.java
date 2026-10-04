package com.doorguard.backend.model;

import java.time.Instant;

public record DashboardSummary(
        int totalEvents,
        int normalCount,
        int unusualCount,
        int criticalCount,
        int activeAlerts,
        int devicesOnline,
        String systemStatus,
        int noiseFiltered,
        String dailySummary,
        String summarySource,
        String privacyNote,
        RingEvent latestCriticalEvent,
        Instant lastEventTime
) {
}