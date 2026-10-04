package com.doorguard.backend.service;

import com.doorguard.backend.model.CreateEventRequest;
import com.doorguard.backend.model.FeedbackResponse;
import com.doorguard.backend.model.RingEvent;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.Instant;
import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

@Service
public class EventService {

    private final List<RingEvent> events = new CopyOnWriteArrayList<>();
    private final AtomicLong idCounter = new AtomicLong(0);

    private final RiskScoringService scoring;
    private final AnalysisService analysis;
    private final LearningService learning;
    private final EventFormatter formatter;

    public EventService(RiskScoringService scoring, AnalysisService analysis,
                        LearningService learning, EventFormatter formatter) {
        this.scoring = scoring;
        this.analysis = analysis;
        this.learning = learning;
        this.formatter = formatter;
        seed();
    }

    private void seed() {
        create(new CreateEventRequest("Front Door Camera", "MOTION", "Front Door", "ANIMAL",
                5, false, "Cat walked across the porch", todayAt(9, 5)));
        create(new CreateEventRequest("Front Door Doorbell", "DOORBELL_PRESSED", "Front Door", "DELIVERY_PERSON",
                20, false, "Delivery person rang the bell and left a parcel", todayAt(15, 12)));
        create(new CreateEventRequest("Front Door Camera", "MOTION", "Front Door", "FAMILIAR_PERSON",
                10, false, "Family member arrived home", todayAt(18, 20)));
        create(new CreateEventRequest("Side Gate Camera", "MOTION", "Side Gate", "UNKNOWN_PERSON",
                12, false, "Unfamiliar person walked past the side gate", todayAt(19, 30)));
        create(new CreateEventRequest("Porch Camera", "PACKAGE_REMOVED", "Porch", "UNKNOWN_PERSON",
                15, false, "Unknown person picked up the parcel from the porch", todayAt(21, 45)));
        create(new CreateEventRequest("Front Door Camera", "MOTION", "Front Door", "UNKNOWN_PERSON",
                45, true, "Unknown person approached the door and tried the handle", todayAt(2, 14)));
    }

    /** Most recent occurrence of the given local time that is not in the future. */
    private Instant todayAt(int hour, int minute) {
        ZonedDateTime now = ZonedDateTime.now(formatter.zone());
        ZonedDateTime t = now.with(LocalTime.of(hour, minute)).withSecond(0).withNano(0);
        if (t.isAfter(now)) {
            t = t.minusDays(1);
        }
        return t.toInstant();
    }

    public List<RingEvent> getAllEvents() {
        return events.stream()
                .sorted(Comparator.comparing(RingEvent::timestamp).reversed())
                .toList();
    }

    public Optional<RingEvent> findById(long id) {
        return events.stream().filter(e -> e.id() == id).findFirst();
    }

    private RingEvent getOrThrow(long id) {
        return findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Event " + id + " not found"));
    }

    public RingEvent create(CreateEventRequest r) {
        RingEvent base = new RingEvent(
                idCounter.incrementAndGet(),
                r.deviceName() != null ? r.deviceName() : "Unknown Device",
                r.eventType(),
                r.location() != null ? r.location() : "Front Door",
                r.visitorType() != null ? r.visitorType() : "UNKNOWN_PERSON",
                r.dwellSeconds() != null ? r.dwellSeconds() : 0,
                r.triedDoor() != null && r.triedDoor(),
                r.description() != null && !r.description().isBlank() ? r.description() : r.eventType(),
                r.timestamp() != null ? r.timestamp() : Instant.now(),
                0, "NORMAL", List.of(), "", "TEMPLATE", List.of(), "NEW");

        RiskScoringService.ScoreResult result = scoring.score(base);
        RingEvent scored = base.withScoring(
                result.score(), result.level(), result.factors(),
                analysis.templateAnalysis(base, result),
                analysis.recommendedActions(base, result.level()));
        events.add(scored);
        return scored;
    }

    /** Returns the event with an LLM-written analysis (cached after the first call). */
    public RingEvent getAnalysis(long id) {
        RingEvent e = getOrThrow(id);
        if ("LLM".equals(e.analysisSource())) {
            return e;
        }
        return analysis.llmAnalysis(e)
                .map(text -> replace(e.withAnalysis(text, "LLM")))
                .orElse(e);
    }

    /** Mark Safe / Confirm Threat: updates the event AND teaches the scoring engine. */
    public FeedbackResponse applyFeedback(long id, String verdict) {
        RingEvent e = getOrThrow(id);
        String v = verdict == null ? "" : verdict.trim().toUpperCase();
        return switch (v) {
            case "SAFE" -> {
                learning.learnSafe(e.location(), e.visitorType());
                RingEvent updated = replace(e.withStatus("SAFE"));
                yield new FeedbackResponse("Marked safe. DoorGuard will lower the risk of similar "
                        + e.visitorType() + " activity at " + e.location() + ".", updated);
            }
            case "THREAT" -> {
                learning.learnThreat(e.location(), e.visitorType());
                RingEvent updated = replace(e.withStatus("THREAT_CONFIRMED"));
                yield new FeedbackResponse("Threat confirmed. DoorGuard will raise the risk of similar "
                        + e.visitorType() + " activity at " + e.location() + ".", updated);
            }
            default -> throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "verdict must be SAFE or THREAT");
        };
    }

    private RingEvent replace(RingEvent updated) {
        for (int i = 0; i < events.size(); i++) {
            if (events.get(i).id().equals(updated.id())) {
                events.set(i, updated);
                break;
            }
        }
        return updated;
    }
}