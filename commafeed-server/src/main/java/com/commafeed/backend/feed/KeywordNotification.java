package com.commafeed.backend.feed;

import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedSubscription;

public record KeywordNotification(FeedSubscription subscription, FeedEntry entry, String keyword) {}
