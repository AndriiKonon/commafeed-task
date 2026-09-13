package com.commafeed.backend.dao;

import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedEntryNote;
import com.commafeed.backend.model.QFeedEntryNote;
import com.commafeed.backend.model.User;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.persistence.EntityManager;

import java.util.List;
import java.util.Optional;

@ApplicationScoped
public class FeedEntryNoteDAO extends GenericDAO<FeedEntryNote> {

    private static final QFeedEntryNote NOTE = QFeedEntryNote.feedEntryNote;

    protected FeedEntryNoteDAO() {
        super(null, FeedEntryNote.class);
    }

    @Inject
    public FeedEntryNoteDAO(EntityManager entityManager) {
        super(entityManager, FeedEntryNote.class);
    }

    public Optional<FeedEntryNote> findByEntry(User user, FeedEntry entry) {
        return Optional.ofNullable(
                query().selectFrom(NOTE)
                        .where(NOTE.user.eq(user), NOTE.feedEntry.eq(entry))
                        .fetchOne());
    }

    public List<FeedEntryNote> findByUser(User user) {
        return query().selectFrom(NOTE).where(NOTE.user.eq(user)).fetch();
    }
}
