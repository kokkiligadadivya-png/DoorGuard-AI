package com.doorguard.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

@Service
public class LlmService {

    private static final Logger log = LoggerFactory.getLogger(LlmService.class);

    private final HttpClient http = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(5)).build();
    private final ObjectMapper mapper;
    private final String apiKey;
    private final String model;

    public LlmService(ObjectMapper mapper,
                      @Value("${doorguard.ai.api-key:}") String apiKey,
                      @Value("${doorguard.ai.model:claude-sonnet-5-5}") String model) {
        this.mapper = mapper;
        this.apiKey = apiKey;
        this.model = model;
        log.info("LlmService started. AI enabled: {}, model: {}", isEnabled(), model);
    }

    public boolean isEnabled() {
        return apiKey != null && !apiKey.isBlank();
    }

    public Optional<String> complete(String system, String user, int maxTokens) {
        if (!isEnabled()) {
            return Optional.empty();
        }
        try {
            ObjectNode body = mapper.createObjectNode();
            body.put("model", model);
            body.put("max_tokens", maxTokens);
            body.put("system", system);
            ArrayNode messages = body.putArray("messages");
            ObjectNode msg = messages.addObject();
            msg.put("role", "user");
            msg.put("content", user);

            HttpRequest request = HttpRequest.newBuilder(URI.create("https://api.anthropic.com/v1/messages"))
                    .timeout(Duration.ofSeconds(20))
                    .header("content-type", "application/json")
                    .header("x-api-key", apiKey)
                    .header("anthropic-version", "2023-06-01")
                    .POST(HttpRequest.BodyPublishers.ofString(mapper.writeValueAsString(body)))
                    .build();

            HttpResponse<String> response = http.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                log.warn("LLM call failed: HTTP {} {}", response.statusCode(), response.body());
                return Optional.empty();
            }
            JsonNode root = mapper.readTree(response.body());
            String text = root.path("content").path(0).path("text").asText(null);
            return Optional.ofNullable(text).filter(t -> !t.isBlank());
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        } catch (Exception ex) {
            log.warn("LLM call error: {}", ex.getMessage());
            return Optional.empty();
        }
    }
}