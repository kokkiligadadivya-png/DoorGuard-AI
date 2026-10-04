package com.doorguard.backend.service;

import com.doorguard.backend.model.AssistantAnswer;
import com.doorguard.backend.model.RingEvent;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AssistantService {

    private static final String SYSTEM = """
            You are the DoorGuard AI assistant for a home security system.
            Answer ONLY from the event log provided. If the log does not contain the answer, say so.
            Be concise (max 4 sentences), mention times and locations, and never invent events.
            Do not use markdown.
            """;

    private final EventService eventService;
    private final LlmService llm;
    private final EventFormatter formatter;

    public AssistantService(EventService eventService, LlmService llm, EventFormatter formatter) {
        this.eventService = eventService;
        this.llm = llm;
        this.formatter = formatter;
    }

    public AssistantAnswer ask(String question) {
        List<RingEvent> all = eventService.getAllEvents();
        List<RingEvent> related = findRelated(question, all);
        List<Long> ids = related.stream().map(RingEvent::id).toList();

        String log = all.stream().map(formatter::line).collect(Collectors.joining("\n"));
        Optional<String> ai = llm.complete(SYSTEM,
                "Event log:\n" + log + "\n\nQuestion: " + question, 500);

        if (ai.isPresent()) {
            return new AssistantAnswer(ai.get(), "LLM", ids);
        }
        return new AssistantAnswer(fallbackAnswer(related), "RULES", ids);
    }

    private List<RingEvent> findRelated(String question, List<RingEvent> all) {
        String q = question.toLowerCase();
        if (q.contains("deliver") || q.contains("package") || q.contains("parcel")) {
            return all.stream().filter(e -> "DELIVERY_PERSON".equals(e.visitorType())
                    || e.eventType().contains("PACKAGE")).toList();
        }
        if (q.contains("unknown") || q.contains("stranger") || q.contains("suspicious")) {
            return all.stream().filter(e -> "UNKNOWN_PERSON".equals(e.visitorType())).toList();
        }
        if (q.contains("critical") || q.contains("dangerous") || q.contains("threat")) {
            return all.stream().filter(e -> "CRITICAL".equals(e.riskLevel())).toList();
        }
        if (q.contains("night")) {
            return all.stream().filter(e -> {
                int h = formatter.hour(e.timestamp());
                return h >= 22 || h < 5;
            }).toList();
        }
        if (q.contains("unusual")) {
            return all.stream().filter(e -> !"NORMAL".equals(e.riskLevel())).toList();
        }
        return all;
    }

    private String fallbackAnswer(List<RingEvent> related) {
        if (related.isEmpty()) {
            return "I found no matching events.";
        }
        String lines = related.stream().limit(5)
                .map(e -> "- " + formatter.clock(e.timestamp()) + " " + e.location() + ": "
                        + e.description() + " (risk " + e.riskScore() + ")")
                .collect(Collectors.joining("\n"));
        return "I found " + related.size() + " matching event(s):\n" + lines;
    }
}