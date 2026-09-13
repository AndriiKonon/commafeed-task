package com.commafeed.backend.service;

import com.commafeed.backend.dao.FeedEntryDAO;
import com.commafeed.backend.dao.FeedSubscriptionDAO;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.User;
import com.commafeed.frontend.model.AlternativeTextResponse;

import jakarta.enterprise.context.ApplicationScoped;

import lombok.RequiredArgsConstructor;

import java.util.Locale;
import java.util.Optional;

@RequiredArgsConstructor
@ApplicationScoped
public class FeedEntryAlternativeService {

    private final FeedEntryDAO feedEntryDAO;
    private final FeedSubscriptionDAO feedSubscriptionDAO;
    private final LlmService llmService;

    public Optional<AlternativeTextResponse> generate(
            User user, Long entryId, String target, String prompt) {
        FeedEntry entry = feedEntryDAO.findById(entryId);
        if (entry == null || feedSubscriptionDAO.findByFeed(user, entry.getFeed()) == null) {
            return Optional.empty();
        }

        String normalizedTarget = target.toLowerCase(Locale.ROOT);
        String original =
                switch (normalizedTarget) {
                    case "title" -> entry.getContent().getTitle();
                    case "content" -> entry.getContent().getContent();
                    default ->
                            throw new IllegalArgumentException("target must be title or content");
                };
        if (original == null || original.isBlank()) {
            throw new IllegalArgumentException("entry target is empty");
        }

        String alternative = llmService.generate(original, prompt);
        AlternativeTextResponse response =
                new AlternativeTextResponse(
                        new AlternativeTextResponse.EntrySnapshot(
                                entry.getId(),
                                entry.getContent().getTitle(),
                                entry.getContent().getContent()),
                        normalizedTarget,
                        prompt,
                        alternative);
        return Optional.of(response);
    }
}
