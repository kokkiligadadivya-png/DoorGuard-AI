package com.doorguard.backend.controller;

import com.doorguard.backend.model.AssistantAnswer;
import com.doorguard.backend.model.AssistantRequest;
import com.doorguard.backend.service.AssistantService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/assistant")
public class AssistantController {

    private final AssistantService assistantService;

    public AssistantController(AssistantService assistantService) {
        this.assistantService = assistantService;
    }

    @PostMapping("/ask")
    public AssistantAnswer ask(@RequestBody AssistantRequest request) {
        if (request.question() == null || request.question().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "question is required");
        }
        return assistantService.ask(request.question());
    }
}