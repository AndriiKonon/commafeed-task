package com.commafeed.backend.service;

import com.commafeed.backend.dao.FeedEntryDAO;
import com.commafeed.backend.dao.FeedEntryNoteDAO;
import com.commafeed.backend.model.FeedEntry;
import com.commafeed.backend.model.FeedEntryNote;
import com.commafeed.backend.model.User;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

@ExtendWith(MockitoExtension.class)
class FeedEntryNoteServiceTest {

    @Mock private FeedEntryNoteDAO feedEntryNoteDAO;
    @Mock private FeedEntryDAO feedEntryDAO;

    @InjectMocks private FeedEntryNoteService feedEntryNoteService;

    @Test
    @DisplayName("saveOrUpdateNote_createsNewNote_whenNoteDoesNotExist")
    void saveOrUpdateNoteCreatesNewNoteWhenNoteDoesNotExist() {
        User user = new User();
        FeedEntry entry = new FeedEntry();
        Long entryId = 42L;

        Mockito.when(feedEntryDAO.findById(entryId)).thenReturn(entry);
        Mockito.when(feedEntryNoteDAO.findByEntry(user, entry)).thenReturn(Optional.empty());

        feedEntryNoteService.saveOrUpdateNote(user, entryId, "A note", 5);

        ArgumentCaptor<FeedEntryNote> noteCaptor = ArgumentCaptor.forClass(FeedEntryNote.class);
        Mockito.verify(feedEntryNoteDAO).persist(noteCaptor.capture());
        FeedEntryNote note = noteCaptor.getValue();
        Assertions.assertSame(user, note.getUser());
        Assertions.assertSame(entry, note.getFeedEntry());
        Assertions.assertEquals("A note", note.getComment());
        Assertions.assertEquals(5, note.getRating());
        Assertions.assertNotNull(note.getCreated());
        Mockito.verify(feedEntryNoteDAO, Mockito.never()).merge(Mockito.any());
    }

    @Test
    @DisplayName("saveOrUpdateNote_updatesExistingNote_whenNoteExists")
    void saveOrUpdateNoteUpdatesExistingNoteWhenNoteExists() {
        User user = new User();
        FeedEntry entry = new FeedEntry();
        FeedEntryNote note = new FeedEntryNote();
        note.setId(1L);
        note.setComment("Old note");
        note.setRating(1);

        Mockito.when(feedEntryDAO.findById(42L)).thenReturn(entry);
        Mockito.when(feedEntryNoteDAO.findByEntry(user, entry)).thenReturn(Optional.of(note));

        feedEntryNoteService.saveOrUpdateNote(user, 42L, "Updated note", 4);

        Assertions.assertEquals("Updated note", note.getComment());
        Assertions.assertEquals(4, note.getRating());
        Mockito.verify(feedEntryNoteDAO).merge(note);
        Mockito.verify(feedEntryNoteDAO, Mockito.never()).persist(Mockito.any());
    }

    @Test
    @DisplayName("getNote_returnsNote_whenFound")
    void getNoteReturnsNoteWhenFound() {
        User user = new User();
        FeedEntry entry = new FeedEntry();
        FeedEntryNote note = new FeedEntryNote();

        Mockito.when(feedEntryDAO.findById(42L)).thenReturn(entry);
        Mockito.when(feedEntryNoteDAO.findByEntry(user, entry)).thenReturn(Optional.of(note));

        Optional<FeedEntryNote> result = feedEntryNoteService.getNote(user, 42L);

        Assertions.assertEquals(Optional.of(note), result);
    }

    @Test
    @DisplayName("deleteNote_removesNote_whenCalled")
    void deleteNoteRemovesNoteWhenCalled() {
        User user = new User();
        FeedEntry entry = new FeedEntry();
        FeedEntryNote note = new FeedEntryNote();

        Mockito.when(feedEntryDAO.findById(42L)).thenReturn(entry);
        Mockito.when(feedEntryNoteDAO.findByEntry(user, entry)).thenReturn(Optional.of(note));

        feedEntryNoteService.deleteNote(user, 42L);

        Mockito.verify(feedEntryNoteDAO).delete(note);
    }
}
