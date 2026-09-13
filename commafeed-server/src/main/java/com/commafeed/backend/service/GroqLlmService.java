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

    private static final String API_KEY_ENV = "GEMINI_API_KEY";
    private static final String API_URL =
            "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=";

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
            ArrayNode contents = requestBody.putArray("contents");
            ObjectNode parts = contents.addObject().putArray("parts").addObject();
            parts.put("text", input + " " + prompt);

            HttpRequest request =
                    HttpRequest.newBuilder()
                            .uri(URI.create(API_URL + apiKey))
                            .timeout(Duration.ofSeconds(30))
                            .header("Content-Type", "application/json")
                            .POST(
                                    HttpRequest.BodyPublishers.ofString(
                                            objectMapper.writeValueAsString(requestBody)))
                            .build();

            HttpResponse<String> response =
                    HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                log.warn("Gemini returned HTTP status {}", response.statusCode());
                throw new LlmServiceException(
                        LlmServiceException.Kind.UPSTREAM_FAILURE, "LLM provider request failed");
            }

            JsonNode content =
                    objectMapper.readTree(response.body()).at("/candidates/0/content/parts/0/text");
            if (!content.isTextual() || content.asText().isBlank()) {
                throw new LlmServiceException(
                        LlmServiceException.Kind.UPSTREAM_FAILURE,
                        "Gemini returned an invalid response");
            }
            return content.asText();
        } catch (LlmServiceException e) {
            throw e;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new LlmServiceException(
                    LlmServiceException.Kind.UPSTREAM_FAILURE, "Gemini request was interrupted", e);
        } catch (IOException | RuntimeException e) {
            throw new LlmServiceException(
                    LlmServiceException.Kind.UPSTREAM_FAILURE, "Gemini request failed", e);
        }
    }
}
