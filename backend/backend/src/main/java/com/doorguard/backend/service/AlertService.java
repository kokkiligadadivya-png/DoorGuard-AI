package com.doorguard.backend.service;

import com.doorguard.backend.model.Alert;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AlertService {

    private final EventService eventService;

    public AlertService(EventService eventService) {
        this.eventService = eventService;
    }

    public List<Alert> getAllAlerts() {
        return eventService.getAllEvents().stream()
                .filter(e -> !"NORMAL".equals(e.riskLevel()))
                .map(e -> new Alert(
                        e.id(),
                        e.id(),
                        e.eventType().replace('_', ' ') + " at " + e.location(),
                        e.riskLevel(),
                        e.aiAnalysis(),
                        "SAFE".equals(e.status()) ? "RESOLVED" : "ACTIVE",
                        e.riskScore(),
                        e.timestamp()))
                .toList();
    }
}