package com.doorguard.backend.service;

import com.doorguard.backend.model.Alert;
import com.doorguard.backend.model.DashboardSummary;
import com.doorguard.backend.model.RingEvent;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class DashboardService {

    private static final String SYSTEM = """
            You are DoorGuard AI. Write a 2-3 sentence daily security summary for the homeowner.
            Mention the most important incident first. Use only the event log. No markdown.
            """;

    private record SummaryCache(String key, String text, String source) {
    }

    private final EventService eventService;
    private final AlertService alertService;
    private final LlmService llm;
    private final EventFormatter formatter;
    private SummaryCache cache;

    public DashboardService(EventService eventService, AlertService alertService,
                            LlmService llm, EventFormatter formatter) {
        this.eventService = eventService;
        this.alertService = alertService;
        this.llm = llm;
        this.formatter = formatter;
    }

    public synchronized DashboardSummary getSummary() {
        List<RingEvent> events = eventService.getAllEvents();
        List<Alert> alerts = alertService.getAllAlerts();

        int normal = (int) events.stream().filter(e -> "NORMAL".equals(e.riskLevel())).count();
        int unusual = (int) events.stream().filter(e -> "UNUSUAL".equals(e.riskLevel())).count();
        int critical = (int) events.stream().filter(e -> "CRITICAL".equals(e.riskLevel())).count();
        int activeAlerts = (int) alerts.stream().filter(a -> "ACTIVE".equals(a.status())).count();

        RingEvent latestCritical = events.stream()
                .filter(e -> "CRITICAL".equals(e.riskLevel()) && "NEW".equals(e.status()))
                .findFirst().orElse(null);

        String status = latestCritical != null ? "CRITICAL"
                : activeAlerts > 0 ? "ATTENTION_NEEDED" : "ALL_CLEAR";

        Instant last = events.stream().map(RingEvent::timestamp).max(Comparator.naturalOrder()).orElse(null);
        SummaryCache summary = dailySummary(events, normal, unusual, critical);

        // noiseFiltered is SIMULATED: a real system would count pets/wind/cars filtered out by AI.
        return new DashboardSummary(events.size(), normal, unusual, critical, activeAlerts, 3, status,
                41 + normal, summary.text(), summary.source(),
                "Faces are not stored. Matching uses anonymous embeddings (planned).",
                latestCritical, last);
    }

    private SummaryCache dailySummary(List<RingEvent> events, int normal, int unusual, int critical) {
        String key = events.stream().map(e -> e.id() + ":" + e.status()).collect(Collectors.joining(","));
        if (cache != null && cache.key().equals(key)) {
            return cache;
        }
        String log = events.stream().map(formatter::line).collect(Collectors.joining("\n"));
        Optional<String> ai = llm.complete(SYSTEM, "Event log:\n" + log, 300);

        SummaryCache fresh;
        if (ai.isPresent()) {
            fresh = new SummaryCache(key, ai.get(), "LLM");
        } else {
            String top = events.stream().max(Comparator.comparingInt(RingEvent::riskScore))
                    .map(e -> e.description() + " (score " + e.riskScore() + ")")
                    .orElse("none");
            fresh = new SummaryCache(key, events.size() + " events recorded: " + normal + " normal, "
                    + unusual + " unusual, " + critical + " critical. Highest risk: " + top + ".", "TEMPLATE");
        }
        cache = fresh;
        return fresh;
    }
}