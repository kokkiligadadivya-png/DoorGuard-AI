package com.doorguard.backend.controller;

import com.doorguard.backend.model.CreateEventRequest;
import com.doorguard.backend.model.FeedbackRequest;
import com.doorguard.backend.model.FeedbackResponse;
import com.doorguard.backend.model.RingEvent;
import com.doorguard.backend.service.EventService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public List<RingEvent> getEvents() {
        return eventService.getAllEvents();
    }

    @GetMapping("/{id}")
    public RingEvent getEvent(@PathVariable long id) {
        return eventService.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Event " + id + " not found"));
    }

    @GetMapping("/{id}/analysis")
    public RingEvent getAnalysis(@PathVariable long id) {
        return eventService.getAnalysis(id);
    }

    @PostMapping
    public ResponseEntity<RingEvent> createEvent(@RequestBody CreateEventRequest request) {
        if (request.eventType() == null || request.eventType().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "eventType is required");
        }
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.create(request));
    }

    @PostMapping("/{id}/feedback")
    public FeedbackResponse feedback(@PathVariable long id, @RequestBody FeedbackRequest request) {
        return eventService.applyFeedback(id, request.verdict());
    }
}