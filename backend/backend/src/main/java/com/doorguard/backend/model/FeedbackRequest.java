package com.doorguard.backend.model;

public record FeedbackRequest(String verdict) {   // "SAFE" or "THREAT"
}