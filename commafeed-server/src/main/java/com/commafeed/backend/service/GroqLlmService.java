package com.commafeed.backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import jakarta.enterprise.context.ApplicationScoped;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

@Slf4j
@RequiredArgsConstructor
@ApplicationScoped
public class GroqLlmService implements LlmService {

    private static final String API_KEY_ENV = "GROQ_API_KEY";
    private static final String API_URL_ENV = "GROQ_API_URL";
    private static final String MODEL_ENV = "GROQ_MODEL";
    private static final String DEFAULT_API_URL = "https://api.groq.com/openai/v1/chat/completions";
    private static final String DEFAULT_MODEL = "llama-3.1-8b-instant";

    private final ObjectMapper objectMapper;

    @Override
    public String generate(String input, String prompt) throws LlmServiceException {
        String apiKey = System.getenv(API_KEY_ENV);
        if (apiKey == null || apiKey.isBlank()) {
            throw new LlmServiceException(
                    LlmServiceException.Kind.UNAVAILABLE, "LLM provider is not configured");
        }

        try {
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", environmentValue(MODEL_ENV, DEFAULT_MODEL));
            requestBody.put("temperature", 0.7);

            ArrayNode messages = requestBody.putArray("messages");
            messages.addObject().put("role", "system").put("content", prompt);
            messages.addObject().put("role", "user").put("content", input);

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(environmentValue(API_URL_ENV, DEFAULT_API_URL)))
                            .timeout(Duration.ofSeconds(30))
                            .header("Authorization", "Bearer " + apiKey)
                            .header("Content-Type", "application/json")
                            .POST(
                                    HttpRequest.BodyPublishers.ofString(
                                            objectMapper.writeValueAsString(requestBody)))
                            .build();

            HttpResponse<String> response =
                    HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.warn("LLM provider returned HTTP status {}", response.statusCode());
                throw new LlmServiceException(
                        LlmServiceException.Kind.UPSTREAM_FAILURE, "LLM provider request failed");
            }

            JsonNode content =
                    objectMapper.readTree(response.body()).at("/choices/0/message/content");
            if (!content.isTextual() || content.asText().isBlank()) {
                throw new LlmServiceException(
                        LlmServiceException.Kind.UPSTREAM_FAILURE,
                        "LLM provider returned an invalid response");
            }
            return content.asText();
        } catch (LlmServiceException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LlmServiceException(
                    LlmServiceException.Kind.UPSTREAM_FAILURE,
                    "LLM provider request was interrupted",
                    e);
        } catch (IOException | RuntimeException e) {
            throw new LlmServiceException(
                    LlmServiceException.Kind.UPSTREAM_FAILURE, "LLM provider request failed", e);
        }
    }

    private static String environmentValue(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }
}
