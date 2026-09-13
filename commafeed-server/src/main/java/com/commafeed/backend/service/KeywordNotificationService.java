package com.commafeed.backend.service;

import com.commafeed.backend.feed.KeywordNotification;
import com.commafeed.backend.feed.KeywordNotificationChannel;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedSubscription;

import jakarta.annotation.PreDestroy;
import jakarta.inject.Singleton;

import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Singleton
public class KeywordNotificationService {

    private static final String KEYWORDS_ENV = "COMMAFEED_KEYWORD_NOTIFICATION_KEYWORDS";

    private final KeywordNotificationChannel channel;
    private final ExecutorService executor;

    public KeywordNotificationService(KeywordNotificationChannel channel) {
        this.channel = channel;
        this.executor = Executors.newCachedThreadPool();
    }

    public void notifyAsync(FeedSubscription subscription, List<FeedEntry> entries) {
        List<String> keywords = configuredKeywords();
        if (keywords.isEmpty() || entries.isEmpty()) {
            return;
        }

        executor.execute(
                () -> {
                    try {
                        for (FeedEntry entry : entries) {
                            for (String keyword : keywords) {
                                if (matches(entry, keyword)) {
                                    notifySafely(
                                            new KeywordNotification(subscription, entry, keyword));
                                }
                            }
                        }
                    } catch (RuntimeException e) {
                        log.warn(
                                "keyword notification matching failed for feed {}: {}",
                                subscription.getFeed().getId(),
                                e.getMessage());
                    }
                });
    }

    private void notifySafely(KeywordNotification notification) {
        try {
            channel.notify(notification);
        } catch (RuntimeException e) {
            log.warn(
                    "keyword notification failed for entry {} and user {}: {}",
                    notification.entry().getId(),
                    notification.subscription().getUser().getId(),
                    e.getMessage());
        }
    }

    private static boolean matches(FeedEntry entry, String keyword) {
        String title =
                entry.getContent() == null
                        ? ""
                        : Jsoup.parse(StringUtils.defaultString(entry.getContent().getTitle()))
                                .text();
        String content =
                entry.getContent() == null
                        ? ""
                        : Jsoup.parse(StringUtils.defaultString(entry.getContent().getContent()))
                                .text();
        String searchableText = (title + "\n" + content).toLowerCase(Locale.ROOT);
        return searchableText.contains(keyword.toLowerCase(Locale.ROOT));
    }

    private static List<String> configuredKeywords() {
        String configured = System.getenv(KEYWORDS_ENV);
        if (StringUtils.isBlank(configured)) {
            return List.of();
        }

        return Arrays.stream(configured.split(","))
                .map(String::trim)
                .filter(StringUtils::isNotBlank)
                .distinct()
                .toList();
    }

    @PreDestroy
    void stop() {
        executor.shutdownNow();
    }
}
