package com.commafeed.backend.service;

import com.commafeed.backend.dao.FeedEntryDAO;
import com.commafeed.backend.dao.FeedEntryNoteDAO;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedEntryNote;
import com.commafeed.backend.model.User;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.transaction.Transactional;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Slf4j
@RequiredArgsConstructor
@ApplicationScoped
public class FeedEntryNoteService {

    private final FeedEntryNoteDAO feedEntryNoteDAO;
    private final FeedEntryDAO feedEntryDAO;

    @Transactional
    public void saveOrUpdateNote(User user, Long entryId, String comment, Integer rating) {
        FeedEntry entry = feedEntryDAO.findById(entryId);
        if (entry == null) {
            log.warn("could not save note: feed entry {} not found", entryId);
            return;
        }

        FeedEntryNote note =
                feedEntryNoteDAO
                        .findByEntry(user, entry)
                        .orElseGet(
                                () -> {
                                    FeedEntryNote newNote = new FeedEntryNote();
                                    newNote.setUser(user);
                                    newNote.setFeedEntry(entry);
                                    newNote.setCreated(new Date());
                                    return newNote;
                                });
        note.setComment(comment);
        note.setRating(rating);

        if (note.getId() == null) {
            feedEntryNoteDAO.persist(note);
        } else {
            feedEntryNoteDAO.merge(note);
        }
    }

    public Optional<FeedEntryNote> getNote(User user, Long entryId) {
        FeedEntry entry = feedEntryDAO.findById(entryId);
        if (entry == null) {
            return Optional.empty();
        }

        return feedEntryNoteDAO.findByEntry(user, entry);
    }

    public List<FeedEntryNote> getNotes(User user) {
        return feedEntryNoteDAO.findByUser(user);
    }

    @Transactional
    public void deleteNote(User user, Long entryId) {
        FeedEntry entry = feedEntryDAO.findById(entryId);
        if (entry == null) {
            log.warn("could not delete note: feed entry {} not found", entryId);
            return;
        }

        feedEntryNoteDAO.findByEntry(user, entry).ifPresent(feedEntryNoteDAO::delete);
    }
}
