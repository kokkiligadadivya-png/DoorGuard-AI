package com.doorguard.backend.service;

import com.doorguard.backend.model.RingEvent;
import com.doorguard.backend.model.RiskFactor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AnalysisService {

    private static final String SYSTEM = """
            You are DoorGuard AI, a calm home-security analyst.
            Write 2-3 plain sentences explaining what happened and why it is or is not concerning.
            Use only the facts given. Do not invent details. Do not use markdown.
            """;

    private final LlmService llm;
    private final EventFormatter formatter;

    public AnalysisService(LlmService llm, EventFormatter formatter) {
        this.llm = llm;
        this.formatter = formatter;
    }

    public String templateAnalysis(RingEvent e, RiskScoringService.ScoreResult r) {
        String reasons = r.factors().stream()
                .filter(f -> f.points() > 0)
                .sorted(Comparator.comparingInt(RiskFactor::points).reversed())
                .limit(3)
                .map(f -> f.name().toLowerCase())
                .collect(Collectors.joining(", "));
        String text = e.description() + " at " + e.location() + ". Risk score "
                + r.score() + "/100 (" + r.level() + ").";
        if (!reasons.isEmpty()) {
            text += " Main factors: " + reasons + ".";
        }
        return text;
    }

    public List<String> recommendedActions(RingEvent e, String level) {
        List<String> actions = new ArrayList<>();
        switch (level) {
            case "CRITICAL" -> {
                actions.add("Turn on the porch light");
                actions.add("Announce via Alexa: \"Someone is at the door\"");
                actions.add("Notify your emergency contact");
                actions.add("Save the video clip");
            }
            case "UNUSUAL" -> {
                actions.add("Turn on the porch light");
                actions.add("Review the clip when convenient");
            }
            default -> actions.add("No action needed");
        }
        if (e.triedDoor()) {
            actions.add("Check that the door is locked");
        }
        if ("PACKAGE_REMOVED".equals(e.eventType())) {
            actions.add("Check whether the package was yours");
        }
        return actions;
    }

    public Optional<String> llmAnalysis(RingEvent e) {
        String factors = e.riskFactors().stream()
                .map(f -> f.name() + " (" + (f.points() >= 0 ? "+" : "") + f.points() + "): " + f.reason())
                .collect(Collectors.joining("\n"));
        String user = "Event: " + formatter.line(e) + "\nRisk factors:\n" + factors;
        return llm.complete(SYSTEM, user, 250);
    }
}