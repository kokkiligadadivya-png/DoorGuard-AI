package com.doorguard.backend.model;

import java.util.List;

public record AssistantAnswer(String answer, String source, List<Long> relatedEventIds) {
}