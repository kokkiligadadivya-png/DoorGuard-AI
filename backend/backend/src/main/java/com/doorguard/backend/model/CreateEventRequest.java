package com.doorguard.backend.model;

import java.time.Instant;

public record CreateEventRequest(
        String deviceName,
        String eventType,
        String location,
        String visitorType,
        Integer dwellSeconds,
        Boolean triedDoor,
        String description,
        Instant timestamp
) {
}