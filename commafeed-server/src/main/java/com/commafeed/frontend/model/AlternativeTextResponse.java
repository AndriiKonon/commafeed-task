package com.commafeed.frontend.model;

public record AlternativeTextResponse(
        EntrySnapshot originalEntry, String target, String prompt, String alternative) {

    public record EntrySnapshot(Long id, String title, String content) {}
}
