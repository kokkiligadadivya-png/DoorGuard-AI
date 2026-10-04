package com.doorguard.backend.service;

import com.doorguard.backend.model.RingEvent;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;

@Component
public class EventFormatter {

    private static final DateTimeFormatter FULL = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final DateTimeFormatter CLOCK = DateTimeFormatter.ofPattern("HH:mm");

    private final ZoneId zone;

    public EventFormatter(@Value("${doorguard.timezone:Asia/Kolkata}") String timezone) {
        this.zone = ZoneId.of(timezone);
    }

    public ZoneId zone() {
        return zone;
    }

    public int hour(Instant t) {
        return t.atZone(zone).getHour();
    }

    public String clock(Instant t) {
        return CLOCK.format(t.atZone(zone));
    }

    /** One compact line per event; this is what the LLM reads. */
    public String line(RingEvent e) {
        return "#%d | %s | %s | %s | %s | %s | stayed %ds | tried door: %s | risk %d %s | status %s | %s".formatted(
                e.id(), FULL.format(e.timestamp().atZone(zone)), e.deviceName(), e.location(),
                e.eventType(), e.visitorType(), e.dwellSeconds(), e.triedDoor(),
                e.riskScore(), e.riskLevel(), e.status(), e.description());
    }
}