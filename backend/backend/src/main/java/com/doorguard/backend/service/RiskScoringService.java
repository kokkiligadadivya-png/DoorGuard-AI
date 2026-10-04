package com.doorguard.backend.service;

import com.doorguard.backend.model.RingEvent;
import com.doorguard.backend.model.RiskFactor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RiskScoringService {

    public record ScoreResult(int score, String level, List<RiskFactor> factors) {
    }

    private final LearningService learning;
    private final EventFormatter formatter;

    public RiskScoringService(LearningService learning, EventFormatter formatter) {
        this.learning = learning;
        this.formatter = formatter;
    }

    public ScoreResult score(RingEvent e) {
        List<RiskFactor> factors = new ArrayList<>();
        String visitor = e.visitorType() == null ? "UNKNOWN_PERSON" : e.visitorType();
        int hour = formatter.hour(e.timestamp());
        boolean night = hour >= 22 || hour < 5;

        switch (visitor) {
            case "UNKNOWN_PERSON" -> factors.add(new RiskFactor("Unknown person", 40,
                    "Not matched to any familiar visitor"));
            case "DELIVERY_PERSON" -> factors.add(new RiskFactor("Delivery person", 5,
                    "Usually harmless, small baseline risk"));
            case "FAMILIAR_PERSON" -> factors.add(new RiskFactor("Familiar person", -10,
                    "Matches a known household member or visitor"));
            default -> { }
        }

        if (night) {
            factors.add(new RiskFactor("Night-time activity", 25,
                    "Activity at " + formatter.clock(e.timestamp()) + ", outside normal hours"));
        }

        // Baseline: deliveries normally arrive 8 AM - 9 PM
        if ("DELIVERY_PERSON".equals(visitor) && (hour < 8 || hour >= 21)) {
            factors.add(new RiskFactor("Unusual delivery time", 20,
                    "Deliveries usually arrive between 8 AM and 9 PM"));
        }

        if (e.dwellSeconds() >= 30) {
            factors.add(new RiskFactor("Lingered at the door", 20,
                    "Stayed " + e.dwellSeconds() + " seconds"));
        } else if (e.dwellSeconds() >= 10) {
            factors.add(new RiskFactor("Paused at the door", 8,
                    "Stayed " + e.dwellSeconds() + " seconds"));
        }

        if (e.triedDoor()) {
            factors.add(new RiskFactor("Tried the door handle", 10, "Physical contact with the door"));
        }

        if ("PACKAGE_REMOVED".equals(e.eventType()) && !"FAMILIAR_PERSON".equals(visitor)) {
            factors.add(new RiskFactor("Package removed", 20, "A package was taken by a non-household visitor"));
        }

        int learned = learning.adjustmentFor(e.location(), visitor);
        if (learned != 0) {
            factors.add(new RiskFactor("Learned from your feedback", learned,
                    learned < 0 ? "You marked similar activity as safe before"
                                : "You confirmed similar activity as a threat before"));
        }

        int total = factors.stream().mapToInt(RiskFactor::points).sum();
        int score = Math.max(0, Math.min(100, total));
        return new ScoreResult(score, levelFor(score), factors);
    }

    public static String levelFor(int score) {
        if (score >= 75) return "CRITICAL";
        if (score >= 35) return "UNUSUAL";
        return "NORMAL";
    }
}