package com.commafeed.backend.feed;

import com.commafeed.CommaFeedConfiguration;
import com.commafeed.backend.HttpClientFactory;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import jakarta.inject.Singleton;

import lombok.extern.slf4j.Slf4j;

import org.apache.hc.client5.http.classic.methods.HttpPost;
import org.apache.hc.client5.http.config.RequestConfig;
import org.apache.hc.client5.http.impl.classic.CloseableHttpClient;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.apache.hc.core5.util.Timeout;

import java.io.IOException;

@Slf4j
@Singleton
public class EnvironmentKeywordNotificationChannel implements KeywordNotificationChannel {

    private static final String CHANNEL_ENV = "COMMAFEED_KEYWORD_NOTIFICATION_CHANNEL";
    private static final String WEBHOOK_URL_ENV = "COMMAFEED_KEYWORD_NOTIFICATION_WEBHOOK_URL";

    private final CloseableHttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final CommaFeedConfiguration config;

    public EnvironmentKeywordNotificationChannel(
            HttpClientFactory httpClientFactory,
            ObjectMapper objectMapper,
            CommaFeedConfiguration config) {
        this.httpClient = httpClientFactory.newClient(1);
        this.objectMapper = objectMapper;
        this.config = config;
    }

    @Override
    public void notify(KeywordNotification notification) {
        String channel = environmentValue(CHANNEL_ENV, "mock");
        if ("mock".equalsIgnoreCase(channel)) {
            log.info(
                    "keyword notification matched keyword '{}' for entry {} and user {}",
                    notification.keyword(),
                    notification.entry().getId(),
                    notification.subscription().getUser().getId());
            return;
        }

        if (!"webhook".equalsIgnoreCase(channel)) {
            log.warn("keyword notification skipped: unsupported channel '{}'", channel);
            return;
        }

        String webhookUrl = System.getenv(WEBHOOK_URL_ENV);
        if (webhookUrl == null || webhookUrl.isBlank()) {
            log.warn("keyword notification skipped: webhook URL is not configured");
            return;
        }

        try {
            ObjectNode payload = objectMapper.createObjectNode();
            payload.put("keyword", notification.keyword());
            payload.put("entryId", notification.entry().getId());
            payload.put("title", notification.entry().getContent().getTitle());
            payload.put("url", notification.entry().getUrl());
            payload.put("feedId", notification.subscription().getFeed().getId());
            payload.put("userId", notification.subscription().getUser().getId());

            HttpPost request = new HttpPost(webhookUrl);
            request.setConfig(
                    RequestConfig.custom()
                            .setResponseTimeout(Timeout.of(config.httpClient().responseTimeout()))
                            .build());
            request.setEntity(
                    new StringEntity(
                            objectMapper.writeValueAsString(payload),
                            ContentType.APPLICATION_JSON));

            httpClient.execute(
                    request,
                    response -> {
                        if (response.getCode() >= 400) {
                            throw new KeywordNotificationException(
                                    "webhook returned status " + response.getCode());
                        }
                        return null;
                    });
        } catch (IOException | RuntimeException e) {
            throw new KeywordNotificationException("webhook notification failed", e);
        }
    }

    private static String environmentValue(String name, String defaultValue) {
        String value = System.getenv(name);
        return value == null || value.isBlank() ? defaultValue : value;
    }

    public static class KeywordNotificationException extends RuntimeException {

        private static final long serialVersionUID = 1L;

        public KeywordNotificationException(String message) {
            super(message);
        }

        public KeywordNotificationException(String message, Throwable cause) {
            super(message, cause);
        }
    }
}
